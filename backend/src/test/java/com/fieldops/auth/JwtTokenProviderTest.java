package com.fieldops.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String VALID_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String STRONG_CUSTOM_SECRET = "c2VjdXJlX2N1c3RvbV9zZWNyZXRfa2V5X2Zvcl9maWVsZG9wc19zdGFnaW5nXzIwMjZfIXh5eg==";

    @Test
    @DisplayName("Startup fails when JWT secret is null or empty")
    void testMissingSecretThrowsException() {
        MockEnvironment env = new MockEnvironment();
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider(null, 3600000L, env));
        assertTrue(ex1.getMessage().contains("JWT secret is not configured"));

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider("   ", 3600000L, env));
        assertTrue(ex2.getMessage().contains("JWT secret is not configured"));
    }

    @Test
    @DisplayName("Startup fails when JWT secret is insufficiently strong (< 256 bits / 32 bytes)")
    void testInsufficientSecretThrowsException() {
        MockEnvironment env = new MockEnvironment();
        // A short 16-character secret (128 bits)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider("shortSecret123!", 3600000L, env));
        assertTrue(ex.getMessage().contains("at least 256 bits (32 bytes)"));
    }

    @Test
    @DisplayName("Startup fails when known default insecure secret is used in staging profile")
    void testDefaultInsecureSecretForbiddenInStaging() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider(JwtTokenProvider.INSECURE_FALLBACK_SECRET, 3600000L, env));
        assertTrue(ex.getMessage().contains("Default insecure JWT secret is strictly forbidden in staging and production"));
    }

    @Test
    @DisplayName("Startup fails when known default insecure secret is used in production profile")
    void testDefaultInsecureSecretForbiddenInProduction() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("production");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider(JwtTokenProvider.INSECURE_FALLBACK_SECRET, 3600000L, env));
        assertTrue(ex.getMessage().contains("Default insecure JWT secret is strictly forbidden in staging and production"));
    }

    @Test
    @DisplayName("Startup succeeds with valid externally supplied secret in staging profile")
    void testValidExternallySuppliedSecretInStaging() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");

        JwtTokenProvider provider = new JwtTokenProvider(STRONG_CUSTOM_SECRET, 3600000L, env);
        assertNotNull(provider);

        String token = provider.generateToken("dispatcher@fieldops.demo");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("dispatcher@fieldops.demo", provider.getUsernameFromJwt(token));
    }

    @Test
    @DisplayName("Token generation, subject extraction, and validity check succeed")
    void testTokenGenerationAndVerification() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600000L, env);
        String token = provider.generateToken("admin@fieldops.demo");

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("admin@fieldops.demo", provider.getUsernameFromJwt(token));

        Date expiration = provider.getExpirationDateFromJwt(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Expired token is rejected by validateToken")
    void testExpiredTokenRejection() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        // Custom expiration of -1000ms (already expired)
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600000L, env);
        String expiredToken = provider.generateToken("technician@fieldops.demo", -1000L);

        assertNotNull(expiredToken);
        assertFalse(provider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Token signed with different secret is rejected (invalid signature)")
    void testInvalidSignatureRejection() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtTokenProvider provider1 = new JwtTokenProvider(VALID_SECRET, 3600000L, env);
        JwtTokenProvider provider2 = new JwtTokenProvider(STRONG_CUSTOM_SECRET, 3600000L, env);

        String tokenFromProvider1 = provider1.generateToken("user@fieldops.demo");

        // Provider 2 has a different signing key, must reject token from Provider 1
        assertFalse(provider2.validateToken(tokenFromProvider1));
    }

    @Test
    @DisplayName("Malformed and arbitrary strings are safely rejected")
    void testMalformedTokenRejection() {
        MockEnvironment env = new MockEnvironment();
        JwtTokenProvider provider = new JwtTokenProvider(VALID_SECRET, 3600000L, env);

        assertFalse(provider.validateToken("not.a.valid.jwt.token"));
        assertFalse(provider.validateToken(""));
        assertFalse(provider.validateToken(null));
        assertFalse(provider.validateToken("eyJhbGciOiJIUzI1NiJ9.malformed.payload"));
    }
}
