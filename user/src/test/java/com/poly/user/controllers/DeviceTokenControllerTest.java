package com.poly.user.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poly.user.models.DeviceToken;
import com.poly.user.models.User;
import com.poly.user.repositories.DeviceTokenRepository;
import com.poly.user.repositories.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@org.springframework.test.annotation.DirtiesContext(
    classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class DeviceTokenControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private DeviceTokenRepository repository;
  @Autowired private com.poly.user.security.JwtService jwtService;

  private String getToken(User user) {
    com.poly.user.security.CustomUserDetails details =
        new com.poly.user.security.CustomUserDetails(user, java.util.List.of("ROLE_ADMIN"));
    return jwtService.generateAccessToken(details);
  }

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void filterAndPaginate_returnsPaginatedResults() throws Exception {
    User user =
        User.builder()
            .email("a@a.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    userRepository.save(user);

    DeviceToken token =
        DeviceToken.builder()
            .user(user)
            .token("token123")
            .platform("IOS")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    repository.save(token);

    mockMvc
        .perform(
            get("/api/devicetokens")
                .param("page", "0")
                .param("size", "10")
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].token").value("token123"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void getById_returnsDeviceToken() throws Exception {
    User user =
        User.builder()
            .email("b@b.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    DeviceToken token =
        DeviceToken.builder().user(user).token("token2").platform("IOS").delIf(false).build();
    token = repository.save(token);

    mockMvc
        .perform(
            get("/api/devicetokens/" + token.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token2"));
  }

  @Test
  void create_returnsCreatedDeviceToken() throws Exception {
    User user =
        User.builder()
            .email("c@c.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                    "/api/devicetokens")
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(
                    "{\"userId\":" + user.getId() + ",\"token\":\"token3\",\"platform\":\"IOS\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("token3"));
  }

  @Test
  void update_returnsUpdatedDeviceToken() throws Exception {
    User user =
        User.builder()
            .email("d@d.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    DeviceToken token =
        DeviceToken.builder().user(user).token("token4").platform("IOS").delIf(false).build();
    token = repository.save(token);

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/devicetokens/" + token.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(
                    "{\"userId\":"
                        + user.getId()
                        + ",\"token\":\"token5\",\"platform\":\"ANDROID\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token5"));
  }

  @Test
  void delete_returnsNoContent() throws Exception {
    User user =
        User.builder()
            .email("f@f.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    DeviceToken token =
        DeviceToken.builder().user(user).token("token6").platform("IOS").delIf(false).build();
    token = repository.save(token);

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/api/devicetokens/" + token.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }
}
