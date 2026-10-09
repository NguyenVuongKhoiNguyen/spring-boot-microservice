package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.poly.user.dtos.requests.LoginRequest;
import com.poly.user.dtos.requests.RefreshRequest;
import com.poly.user.dtos.requests.RegisterRequest;
import com.poly.user.dtos.responses.AuthResponse;
import com.poly.user.dtos.responses.LoginResult;
import com.poly.user.models.RefreshToken;
import com.poly.user.models.Role;
import com.poly.user.models.User;
import com.poly.user.repositories.RefreshTokenRepository;
import com.poly.user.repositories.RoleRepository;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import com.poly.user.security.CustomUserDetails;
import com.poly.user.security.CustomUserDetailsService;
import com.poly.user.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private CustomUserDetailsService userDetailsService;

  @InjectMocks private AuthService authService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
  }

  @Test
  void register_whenEmailNotUsed_returnsAuthResponse() {
    // Arrange
    RegisterRequest req = new RegisterRequest("test@test.com", "password", "Test User", "123456");
    when(userRepository.findByEmailAndDelIfFalse("test@test.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password")).thenReturn("hashed");

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setEmail("test@test.com");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    Role userRole = new Role();
    userRole.setName("USER");
    when(roleRepository.findByNameAndDelIfFalse("USER")).thenReturn(Optional.of(userRole));

    when(jwtService.generateAccessToken(any())).thenReturn("access");
    when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

    // Act
    AuthResponse response = authService.register(req);

    // Assert
    assertThat(response.getAccessToken()).isEqualTo("access");
    assertThat(response.getRefreshToken()).isEqualTo("refresh");
    verify(userRoleRepository).save(any());
    verify(refreshTokenRepository).save(any());
  }

  @Test
  void register_whenEmailUsed_throwsException() {
    // Arrange
    RegisterRequest req = new RegisterRequest("test@test.com", "password", "Test User", "123456");
    when(userRepository.findByEmailAndDelIfFalse("test@test.com"))
        .thenReturn(Optional.of(new User()));

    // Act & Assert
    assertThatThrownBy(() -> authService.register(req))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void login_whenValid_returnsTokens() {
    // Arrange
    LoginRequest req = new LoginRequest("test@test.com", "password");
    User user = new User();
    user.setId(1L);
    user.setEmail("test@test.com");
    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));
    when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
    when(jwtService.generateAccessToken(userDetails)).thenReturn("access");
    when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh");

    // Act
    LoginResult response = authService.login(req);

    // Assert
    assertThat(response.accessToken()).isEqualTo("access");
    assertThat(response.user().email()).isEqualTo("test@test.com");
    verify(authenticationManager).authenticate(any());
    verify(refreshTokenRepository).save(any());
  }

  @Test
  void login_whenUnknownEmail_throwsException() {
    LoginRequest req = new LoginRequest("unknown@test.com", "password");
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(req)).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void login_whenWrongPassword_throwsException() {
    LoginRequest req = new LoginRequest("test@test.com", "wrong");
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(req)).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void login_whenInactiveOrDeleted_throwsException() {
    LoginRequest req = new LoginRequest("test@test.com", "password");
    when(authenticationManager.authenticate(any()))
        .thenThrow(
            new org.springframework.security.authentication.DisabledException("User is disabled"));

    assertThatThrownBy(() -> authService.login(req)).isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void refresh_whenValid_returnsNewTokens() {
    // Arrange
    RefreshRequest req = new RefreshRequest("oldToken");
    User user = new User();
    user.setActive(true);
    user.setDelIf(false);
    user.setEmail("test@test.com");
    RefreshToken stored = new RefreshToken();
    stored.setRevoked(false);
    stored.setExpiresAt(Instant.now().plusSeconds(3600));
    stored.setUser(user);

    when(refreshTokenRepository.findByTokenHashAndDelIfFalse(anyString()))
        .thenReturn(Optional.of(stored));
    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));
    when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
    when(jwtService.generateAccessToken(userDetails)).thenReturn("access2");
    when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh2");

    // Act
    AuthResponse response = authService.refresh(req);

    // Assert
    assertThat(response.getAccessToken()).isEqualTo("access2");
    assertThat(stored.getRevoked()).isTrue();
    verify(refreshTokenRepository, times(2)).save(any());
  }

  @Test
  void refresh_whenRevoked_throwsException() {
    // Arrange
    RefreshRequest req = new RefreshRequest("oldToken");
    RefreshToken stored = new RefreshToken();
    stored.setRevoked(true);

    when(refreshTokenRepository.findByTokenHashAndDelIfFalse(anyString()))
        .thenReturn(Optional.of(stored));

    // Act & Assert
    assertThatThrownBy(() -> authService.refresh(req)).isInstanceOf(BadCredentialsException.class);
  }
}
