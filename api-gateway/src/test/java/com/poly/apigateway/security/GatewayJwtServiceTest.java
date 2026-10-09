package com.poly.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

class GatewayJwtServiceTest {

  private GatewayJwtService jwtService;
  private String secret = "01234567890123456789012345678912";

  @BeforeEach
  void setUp() {
    jwtService = new GatewayJwtService();
    ReflectionTestUtils.setField(jwtService, "secret", secret);
    jwtService.init();
  }

  @Test
  void validateToken_validToken_returnsTrue() {
    String token = Jwts.builder()
        .subject("10")
        .claim("roles", List.of("USER"))
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 900000))
        .id(UUID.randomUUID().toString())
        .issuer("api-gateway")
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();

    assertThat(jwtService.validateToken(token)).isTrue();
    assertThat(jwtService.extractUserId(token)).isEqualTo("10");
    assertThat(jwtService.extractRoles(token)).containsExactly("USER");
  }

  @Test
  void validateToken_invalidToken_returnsFalse() {
    assertThat(jwtService.validateToken("invalid-token")).isFalse();
  }
}
