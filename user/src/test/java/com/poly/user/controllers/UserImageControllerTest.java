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
class UserImageControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private UserImageRepository repository;

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
        .perform(get("/api/userimages").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].imageUrl").value("url1"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }
}
