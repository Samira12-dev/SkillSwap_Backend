package com.example.skillswap.config;

import com.example.skillswap.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {

        User user = (User) userDetails;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .claim("id", user.getId())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("email", user.getEmail())
                .claim("city", user.getCity())
                .claim("photo", user.getPhoto())
                .claim("bio",user.getBio())
                .claim("rating",user.getRating())
                .claim("createdAt",user.getCreatedAt().toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getUserFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }
}