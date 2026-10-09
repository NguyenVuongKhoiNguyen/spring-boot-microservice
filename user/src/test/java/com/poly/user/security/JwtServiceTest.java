package com.poly.user.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.poly.user.models.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(
        jwtService, "secret", "01234567890123456789012345678912"); // 32 bytes
    ReflectionTestUtils.setField(jwtService, "accessExpiration", 900000L);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    jwtService.init();
  }

  @Test
  void token_generation_and_validation() {
    User user = new User();
    user.setId(10L);
    CustomUserDetails details = new CustomUserDetails(user, List.of("USER"));

    String token = jwtService.generateAccessToken(details);

    assertThat(jwtService.validateToken(token)).isTrue();
    assertThat(jwtService.extractUserId(token)).isEqualTo(10L);
    assertThat(jwtService.extractRoles(token)).containsExactly("USER");
  }

  @Test
  void validate_invalidToken_returnsFalse() {
    assertThat(jwtService.validateToken("invalid-token")).isFalse();
  }
}
