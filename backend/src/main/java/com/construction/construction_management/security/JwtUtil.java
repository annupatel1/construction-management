package com.construction.construction_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String TOKEN_VERSION_CLAIM = "ver";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** The token subject is the user id, because users can log in with phone OR email. */
    public String generateToken(int userId, int tokenVersion) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_VERSION_CLAIM, tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Verifies the signature and expiry, then returns the claims. Throws a JWT exception when the
     * token is invalid, so callers can treat every failure as "not logged in".
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public int extractUserId(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    public int extractTokenVersion(Claims claims) {
        Integer version = claims.get(TOKEN_VERSION_CLAIM, Integer.class);
        return version == null ? 0 : version;
    }
}
