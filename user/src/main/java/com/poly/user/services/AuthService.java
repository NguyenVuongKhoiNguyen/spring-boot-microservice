package com.poly.user.services;

import com.poly.user.dtos.requests.LoginRequest;
import com.poly.user.dtos.requests.RefreshRequest;
import com.poly.user.dtos.requests.RegisterRequest;
import com.poly.user.dtos.responses.AuthResponse;
import com.poly.user.dtos.responses.LoginResult;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.models.RefreshToken;
import com.poly.user.models.Role;
import com.poly.user.models.User;
import com.poly.user.models.UserRole;
import com.poly.user.repositories.RefreshTokenRepository;
import com.poly.user.repositories.RoleRepository;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import com.poly.user.security.CustomUserDetails;
import com.poly.user.security.CustomUserDetailsService;
import com.poly.user.security.JwtService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService userDetailsService;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.findByEmailAndDelIfFalse(request.email()).isPresent()) {
      throw new IllegalArgumentException("Email is already in use");
    }

    User user = new User();
    user.setEmail(request.email());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setFullName(request.fullName());
    user.setPhone(request.phone());
    user.setActive(true);
    user.setDelIf(false);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());

    user = userRepository.save(user);

    Role userRole =
        roleRepository
            .findByNameAndDelIfFalse("USER")
            .orElseGet(
                () -> {
                  Role r = new Role();
                  r.setName("USER");
                  r.setDelIf(false);
                  r.setCreatedAt(Instant.now());
                  r.setUpdatedAt(Instant.now());
                  return roleRepository.save(r);
                });

    UserRole ur = new UserRole();
    ur.setUser(user);
    ur.setRole(userRole);
    ur.setDelIf(false);
    ur.setCreatedAt(Instant.now());
    userRoleRepository.save(ur);

    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    saveRefreshToken(user, refreshToken);

    return new AuthResponse(accessToken, refreshToken);
  }

  @Transactional
  public LoginResult login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid email or password");
    }

    CustomUserDetails userDetails =
        (CustomUserDetails) userDetailsService.loadUserByUsername(request.getEmail());

    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    saveRefreshToken(userDetails.getUser(), refreshToken);

    UserResponse userResponse =
        UserResponse.from(
            userDetails.getUser(),
            userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList());

    return new LoginResult(userResponse, accessToken, refreshToken);
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    String tokenHash = hashToken(request.refreshToken());
    RefreshToken storedToken =
        refreshTokenRepository
            .findByTokenHashAndDelIfFalse(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    if (storedToken.getRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
      throw new BadCredentialsException("Refresh token is revoked or expired");
    }

    User user = storedToken.getUser();
    if (Boolean.TRUE.equals(user.getDelIf()) || Boolean.FALSE.equals(user.getActive())) {
      throw new BadCredentialsException("User is not active or deleted");
    }

    // Revoke the old token (rotation)
    storedToken.setRevoked(true);
    refreshTokenRepository.save(storedToken);

    CustomUserDetails userDetails =
        (CustomUserDetails) userDetailsService.loadUserByUsername(user.getEmail());

    String newAccessToken = jwtService.generateAccessToken(userDetails);
    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

    saveRefreshToken(user, newRefreshToken);

    return new AuthResponse(newAccessToken, newRefreshToken);
  }

  @Transactional
  public void logout(String refreshTokenValue) {
    if (refreshTokenValue == null) return;
    String tokenHash = hashToken(refreshTokenValue);
    refreshTokenRepository
        .findByTokenHashAndDelIfFalse(tokenHash)
        .ifPresent(
            token -> {
              token.setRevoked(true);
              refreshTokenRepository.save(token);
            });
  }

  private void saveRefreshToken(User user, String token) {
    String tokenHash = hashToken(token);
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
    refreshToken.setRevoked(false);
    refreshToken.setDelIf(false);
    refreshTokenRepository.save(refreshToken);
  }

  private String hashToken(String token) {
    try {
      java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder(2 * hash.length);
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to hash token", e);
    }
  }
}
