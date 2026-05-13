package com.example.attendancesystem.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AiServiceHealthIndicator implements HealthIndicator {

    private final String aiServerUrl;
    private final RestTemplate restTemplate;

    public AiServiceHealthIndicator(@Value("${ai.server.url}") String aiServerUrl,
                                    RestTemplateBuilder restTemplateBuilder) {
        this.aiServerUrl = aiServerUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Health health() {
        try {
            Map<?, ?> response = restTemplate.getForObject(aiServerUrl + "/health", Map.class);
            return Health.up()
                    .withDetail("service", "ai-engine")
                    .withDetail("url", aiServerUrl)
                    .withDetail("response", response)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("service", "ai-engine")
                    .withDetail("url", aiServerUrl)
                    .build();
        }
    }
}
