package com.poly.user.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.poly.user.models.User;
import com.poly.user.models.UserImage;
import com.poly.user.repositories.UserImageRepository;
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
class UserImageControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private UserImageRepository repository;
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

    UserImage image =
        UserImage.builder()
            .user(user)
            .imageUrl("url1")
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    repository.save(image);

    mockMvc
        .perform(
            get("/api/userimages")
                .param("page", "0")
                .param("size", "10")
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].imageUrl").value("url1"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void getById_returnsUserImage() throws Exception {
    User user =
        User.builder()
            .email("b@b.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    UserImage image = UserImage.builder().user(user).imageUrl("url2").delIf(false).build();
    image = repository.save(image);

    mockMvc
        .perform(
            get("/api/userimages/" + image.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imageUrl").value("url2"));
  }

  @Test
  void create_returnsCreatedUserImage() throws Exception {
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
                    "/api/userimages")
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + user.getId() + ",\"imageUrl\":\"url3\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.imageUrl").value("url3"));
  }

  @Test
  void update_returnsUpdatedUserImage() throws Exception {
    User user =
        User.builder()
            .email("d@d.com")
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .build();
    user = userRepository.save(user);

    UserImage image = UserImage.builder().user(user).imageUrl("url4").delIf(false).build();
    image = repository.save(image);

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/userimages/" + image.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + user.getId() + ",\"imageUrl\":\"url5\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imageUrl").value("url5"));
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

    UserImage image = UserImage.builder().user(user).imageUrl("url6").delIf(false).build();
    image = repository.save(image);

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/api/userimages/" + image.getId())
                .cookie(new jakarta.servlet.http.Cookie("access_token", getToken(user)))
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }
}
