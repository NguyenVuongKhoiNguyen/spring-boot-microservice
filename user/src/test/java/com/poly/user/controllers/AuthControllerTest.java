package com.poly.user.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.user.dtos.requests.LoginRequest;
import com.poly.user.models.Role;
import com.poly.user.models.User;
import com.poly.user.models.UserRole;
import com.poly.user.repositories.RoleRepository;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AuthControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserRoleRepository userRoleRepository;
  @Autowired private com.poly.user.repositories.RefreshTokenRepository refreshTokenRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    refreshTokenRepository.deleteAll();
    userRoleRepository.deleteAll();
    userRepository.deleteAll();
    roleRepository.deleteAll();

    Role role = new Role();
    role.setName("USER");
    role.setDelIf(false);
    role.setCreatedAt(Instant.now());
    role.setUpdatedAt(Instant.now());
    role = roleRepository.save(role);

    User user = new User();
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
  void login_whenValid_returnsCookiesAndBody() throws Exception {
    LoginRequest request = new LoginRequest("test@example.com", "password123");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("access_token"))
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(cookie().path("access_token", "/"))
        .andExpect(cookie().exists("refresh_token"))
        .andExpect(cookie().httpOnly("refresh_token", true))
        .andExpect(cookie().path("refresh_token", "/api/auth"))
        .andExpect(jsonPath("$.accessToken").doesNotExist())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void login_whenWrongPassword_returns401() throws Exception {
    LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(cookie().doesNotExist("access_token"))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void logout_clearsCookies() throws Exception {
    // First, login to get tokens
    LoginRequest request = new LoginRequest("test@example.com", "password123");

    var result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie accessToken = result.getResponse().getCookie("access_token");
    Cookie refreshToken = result.getResponse().getCookie("refresh_token");

    // Then logout with CSRF token
    mockMvc
        .perform(post("/api/auth/logout").cookie(accessToken, refreshToken).with(csrf()))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge("access_token", 0))
        .andExpect(cookie().maxAge("refresh_token", 0));
  }

  @Test
  void post_withoutCsrf_returns403() throws Exception {
    // Test logout without CSRF token
    mockMvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
  }
}
