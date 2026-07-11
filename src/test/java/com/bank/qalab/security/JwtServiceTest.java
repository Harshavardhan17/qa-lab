package com.bank.qalab.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwt =
            new JwtService("test-secret-key-for-junit-only-0123456789-abcdefghijklmnop", 60, 7);

    @Test
    void accessTokenRoundTripsWithClaims() {
        Claims claims = jwt.parse(jwt.generateAccessToken("alice", "CUSTOMER"));
        assertEquals("alice", claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role"));
        assertTrue(jwt.isType(claims, JwtService.TYPE_ACCESS));
        assertFalse(jwt.isType(claims, JwtService.TYPE_REFRESH));
    }

    @Test
    void refreshTokenIsTypedRefresh() {
        Claims claims = jwt.parse(jwt.generateRefreshToken("bob"));
        assertTrue(jwt.isType(claims, JwtService.TYPE_REFRESH));
        assertEquals("bob", claims.getSubject());
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwt.generateAccessToken("alice", "CUSTOMER");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("a") ? "bb" : "aa");
        assertThrows(Exception.class, () -> jwt.parse(tampered));
    }

    @Test
    void shortSecretIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60, 7));
    }
}
