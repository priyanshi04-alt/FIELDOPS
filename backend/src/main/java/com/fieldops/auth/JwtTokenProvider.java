package com.fieldops.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    public static final String INSECURE_FALLBACK_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final int MIN_KEY_BYTES = 32; // 256 bits for HMAC-SHA-256

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs = 86400000L;

    @Autowired(required = false)
    private Environment environment;

    private Key cachedSigningKey;

    public JwtTokenProvider() {
    }

    public JwtTokenProvider(String jwtSecret, long jwtExpirationMs, Environment environment) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.environment = environment;
        validateAndInitializeKey();
    }

    @PostConstruct
    public void validateAndInitializeKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. APP_JWT_SECRET must be supplied.");
        }

        String trimmedSecret = jwtSecret.trim();

        boolean isStagingOrProd = environment != null &&
                (environment.matchesProfiles("staging") ||
                 environment.matchesProfiles("prod") ||
                 environment.matchesProfiles("production"));

        if (isStagingOrProd && INSECURE_FALLBACK_SECRET.equalsIgnoreCase(trimmedSecret)) {
            throw new IllegalStateException("Default insecure JWT secret is strictly forbidden in staging and production profiles. APP_JWT_SECRET must be configured with a secure external secret.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(trimmedSecret);
        } catch (Exception e) {
            keyBytes = trimmedSecret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes) for HMAC-SHA-256.");
        }

        this.cachedSigningKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialized successfully (algorithm: HS256, key length: {} bits)", keyBytes.length * 8);
    }

    private Key getSigningKey() {
        if (this.cachedSigningKey == null) {
            validateAndInitializeKey();
        }
        return this.cachedSigningKey;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername());
    }

    public String generateToken(String username) {
        return generateToken(username, this.jwtExpirationMs);
    }

    public String generateToken(String username, long customExpirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + customExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Date getExpirationDateFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (SecurityException | MalformedJwtException ex) {
            log.warn("Invalid JWT signature or malformed token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Empty or invalid JWT token: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}
