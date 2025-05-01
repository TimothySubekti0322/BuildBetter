package com.buildbetter.shared.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationTime;
    private final JwtParserBuilder parser;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parser().verifyWith(this.secretKey);
        this.expirationTime = 1000L * 60 * 60 * 24 * 7 * 4; // 4 week in milliseconds
    }

    /* ---------- 1) issue token ---------- */
    public String generateToken(String id, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        // Generate a signing key from the secret key
        // Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Map<String, String> claims = new HashMap<>();
        claims.put("role", role.toUpperCase());
        claims.put("id", id);
        claims.put("username", username);

        return Jwts.builder().claims(claims).subject(id)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    /* ---------- 2) read claims ---------- */
    public Claims extractClaims(String token) {
        return this.parser
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /* ---------- 3) short-hands ---------- */
    public boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String getUserId(String token) {
        return extractClaims(token).get("id", String.class);
    }

    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Jws<Claims> validate(String token) throws JwtException {
        return this.parser.build().parseSignedClaims(token);
    }
}
