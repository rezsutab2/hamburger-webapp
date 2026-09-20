package hu.hamburger.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    private static final Path LOCAL_SECRET_FILE = Path.of("jwt-secret.local");

    private Key key;

    private final long EXPIRATION = 1000 * 60 * 60 * 24;

    @PostConstruct
    public void init() {
        String resolvedSecret = (secret != null && !secret.isBlank())
                ? secret
                : loadOrCreateLocalSecret();

        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(resolvedSecret));
    }

    private String loadOrCreateLocalSecret() {
        try {
            if (Files.exists(LOCAL_SECRET_FILE)) {
                String existing = Files.readString(LOCAL_SECRET_FILE).trim();
                if (isValidSecret(existing)) {
                    return existing;
                }
            }

            byte[] randomBytes = new byte[64];
            new SecureRandom().nextBytes(randomBytes);
            String generated = Base64.getEncoder().encodeToString(randomBytes);

            Files.writeString(LOCAL_SECRET_FILE, generated);
            return generated;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Nem sikerült létrehozni/beolvasni a jwt-secret.local fájlt. " +
                            "Alternatívaként állítsd be a JWT_SECRET környezeti változót.", e);
        }
    }

    private boolean isValidSecret(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        try {
            return Base64.getDecoder().decode(candidate).length >= 32;
        } catch (IllegalArgumentException notValidBase64) {
            return false;
        }
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}