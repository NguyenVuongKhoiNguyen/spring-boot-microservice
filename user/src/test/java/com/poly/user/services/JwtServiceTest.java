package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.poly.user.models.User;
import com.poly.user.security.CustomUserDetails;
import com.poly.user.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    // 32 chars minimum
    ReflectionTestUtils.setField(jwtService, "secret", "my-super-secret-key-my-super-secret-key");
    ReflectionTestUtils.setField(jwtService, "accessExpiration", 3600000L);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    jwtService.init();
  }

  @Test
  void generateAccessToken_createsValidToken() {
    User user = new User();
    user.setId(1L);
    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));

    String token = jwtService.generateAccessToken(userDetails);
    assertThat(token).isNotBlank();
    assertTrue(jwtService.validateToken(token));
    assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    assertThat(jwtService.extractRoles(token)).containsExactly("USER");
  }

  @Test
  void generateRefreshToken_createsValidToken() {
    User user = new User();
    user.setId(1L);
    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));

    String token = jwtService.generateRefreshToken(userDetails);
    assertThat(token).isNotBlank();
    assertTrue(jwtService.validateToken(token));
    assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
  }

  @Test
  void validateToken_withInvalidToken_returnsFalse() {
    assertFalse(jwtService.validateToken("invalid.token.here"));
  }
}
