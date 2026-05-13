package com.example.attendancesystem.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.example.attendancesystem.config.SecurityProperties;
import com.example.attendancesystem.dto.LoginRequest;
import com.example.attendancesystem.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SecurityProperties properties;
    private final JwtEncoder jwtEncoder;

    public AuthController(SecurityProperties properties, JwtEncoder jwtEncoder) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (!properties.getAdminUsername().equals(request.username())
                || !properties.getAdminPassword().equals(request.password())) {
            log.warn("Admin login failed for username={}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.getJwtTtlMinutes()));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("attendance-system")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(request.username())
                .claim("scope", "admin")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();

        log.info("Admin login succeeded username={}", request.username());
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", expiresAt));
    }
}
