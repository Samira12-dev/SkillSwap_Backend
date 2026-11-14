package com.example.skillswap.config;

import com.example.skillswap.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    void validateSecret() {
        key();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {

        User user = (User) userDetails;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .claim("id", user.getId())
                .claim("tokenVersion", user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(key())
                .compact();
    }

    public String getUserFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenVersionValid(String token, User user) {
        try {
            Object claim = Jwts.parser()
                    .setSigningKey(key())
                    .parseClaimsJws(token)
                    .getBody()
                    .get("tokenVersion");

            return claim instanceof Number version
                    && version.longValue() == user.getTokenVersion();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key())
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }
}