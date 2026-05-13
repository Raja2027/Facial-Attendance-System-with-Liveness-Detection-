package com.example.attendancesystem.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.attendancesystem.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long WINDOW_SECONDS = 60;

    private final RateLimitProperties properties;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        int limit = limitFor(request);
        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getMethod() + ":" + request.getServletPath() + ":" + clientIp(request);
        if (!allow(key, limit)) {
            log.warn("Rate limit exceeded path={} clientIp={}", request.getServletPath(), clientIp(request));
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int limitFor(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return 0;
        }
        if ("/persons/mark-attendance".equals(request.getServletPath())) {
            return properties.getMarkAttendancePerMinute();
        }
        if ("/persons/register-with-video".equals(request.getServletPath())) {
            return properties.getRegisterPerMinute();
        }
        return 0;
    }

    private boolean allow(String key, int limit) {
        long now = Instant.now().getEpochSecond();
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(now));

        synchronized (counter) {
            if (now - counter.windowStartedAt >= WINDOW_SECONDS) {
                counter.windowStartedAt = now;
                counter.count = 0;
            }
            counter.count++;
            return counter.count <= limit;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class WindowCounter {
        private long windowStartedAt;
        private int count;

        private WindowCounter(long windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }
    }
}
