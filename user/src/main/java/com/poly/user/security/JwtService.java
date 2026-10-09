package com.poly.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-expiration}")
  private long accessExpiration;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  private SecretKey key;

  @PostConstruct
  public void init() {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters long");
    }
    key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(CustomUserDetails user) {
    return buildToken(user, accessExpiration);
  }

  public String generateRefreshToken(CustomUserDetails user) {
    return buildToken(user, refreshExpiration);
  }

  private String buildToken(CustomUserDetails user, long expiration) {
    return Jwts.builder()
        .subject(user.getUser().getId().toString())
        .claim("roles", user.getAuthorities().stream().map(a -> a.getAuthority()).toList())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .id(UUID.randomUUID().toString())
        .issuer("api-gateway")
        .signWith(key)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Long extractUserId(String token) {
    return Long.parseLong(extractAllClaims(token).getSubject());
  }

  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    return extractAllClaims(token).get("roles", List.class);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
