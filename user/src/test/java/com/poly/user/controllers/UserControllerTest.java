package com.poly.user.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.user.models.Role;
import com.poly.user.models.User;
import com.poly.user.models.UserRole;
import com.poly.user.repositories.RoleRepository;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import com.poly.user.security.CustomUserDetails;
import com.poly.user.security.JwtService;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
@Testcontainers
@org.springframework.test.annotation.DirtiesContext(
    classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("integration")
class UserControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserRoleRepository userRoleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;

  private User user;

  @BeforeEach
  void setUp() {
    userRoleRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();

    Role role = new Role();
    role.setName("USER");
    role.setDelIf(false);
    role.setCreatedAt(Instant.now());
    role.setUpdatedAt(Instant.now());
    role = roleRepository.save(role);

    user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash(passwordEncoder.encode("password123"));
    user.setFullName("Test User");
    user.setPhone("123456789");
    user.setActive(true);
    user.setDelIf(false);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    user = userRepository.save(user);

    UserRole userRole = new UserRole();
    userRole.setUser(user);
    userRole.setRole(role);
    userRole.setDelIf(false);
    userRole.setCreatedAt(Instant.now());
    userRoleRepository.save(userRole);
  }

  @Test
  void getMe_withValidCookie_returnsUser() throws Exception {
    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));
    String token = jwtService.generateAccessToken(userDetails);

    mockMvc
        .perform(get("/api/users/me").cookie(new Cookie("access_token", token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId()))
        .andExpect(jsonPath("$.email").value(user.getEmail()));
  }

  @Test
  void getMe_withoutCookie_returns401() throws Exception {
    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.message").value("Full authentication is required to access this resource"));
  }

  @Test
  void getMe_withTamperedCookie_returns401() throws Exception {
    mockMvc
        .perform(get("/api/users/me").cookie(new Cookie("access_token", "invalid.tampered.token")))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.message").value("Full authentication is required to access this resource"));
  }

  @Test
  void getMe_withExpiredCookie_returns401() throws Exception {
    JwtService tempJwtService = new JwtService();
    ReflectionTestUtils.setField(
        tempJwtService, "secret", ReflectionTestUtils.getField(jwtService, "secret"));
    ReflectionTestUtils.setField(
        tempJwtService,
        "accessExpiration",
        -3600000L); // Negative expiration makes it expired immediately
    tempJwtService.init();

    CustomUserDetails userDetails = new CustomUserDetails(user, List.of("USER"));
    String expiredToken = tempJwtService.generateAccessToken(userDetails);

    mockMvc
        .perform(get("/api/users/me").cookie(new Cookie("access_token", expiredToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.message").value("Full authentication is required to access this resource"));
  }
}
