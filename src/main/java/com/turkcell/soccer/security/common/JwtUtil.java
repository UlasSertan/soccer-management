package com.turkcell.soccer.security.common;

import com.turkcell.soccer.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

// Component -> Spring handles lifetime
@Component
public class JwtUtil {

    // Must be 32 bytes for HS256
    private final String secret_key = "a-very-long-random-secret-key-at-least-32-bytes";
    private final SecretKey key = Keys.hmacShaKeyFor(secret_key.getBytes());


    public String generateToken(String username) {
        // Create the key
        return Jwts.builder()
                // Body
                .subject(username)
                // Dates for expiration
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hour Token
                // Sign
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        // Claims: Information carried inside the JWT payload
        // It is called claim because it is not verified until we do so
        // JWT = Header-Payload(Claims is here)-Signature
        Claims claims = Jwts.parser() // Set parser for configuration
                .verifyWith(key) // Give the key for parsing
                .build() // Create the parser
                .parseSignedClaims(token). // Parse the JWT, verify and extract claim
                getPayload(); // Return the claims object

        return claims.getSubject(); // Return the username
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid or expired JWT", e);
        }
    }




}
