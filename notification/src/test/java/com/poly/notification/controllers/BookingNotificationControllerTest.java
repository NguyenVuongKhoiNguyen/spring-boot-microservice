package com.poly.notification.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.notification.dtos.requests.BookingNotificationRequest;
import com.poly.notification.models.BookingNotification;
import com.poly.notification.repositories.BookingNotificationRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookingNotificationControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private BookingNotificationRepository repository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    repository.deleteAllInBatch();
  }

  @Test
  void filterAndPaginate_returnsPaginatedResults() throws Exception {
    BookingNotification notification =
        BookingNotification.builder()
            .userId(100L)
            .title("Welcome")
            .message("Message")
            .type("GENERAL")
            .referenceId(200L)
            .isRead(false)
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    repository.save(notification);

    mockMvc
        .perform(get("/api/notifications").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].title").value("Welcome"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void filterAndPaginate_excludesDeleted() throws Exception {
    BookingNotification notification =
        BookingNotification.builder()
            .userId(100L)
            .title("Welcome")
            .message("Message")
            .type("GENERAL")
            .isRead(false)
            .delIf(true)
            .createdAt(Instant.now())
            .build();
    repository.save(notification);

    mockMvc
        .perform(get("/api/notifications"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void getById_whenExists_returns200() throws Exception {
    BookingNotification notification =
        repository.save(
            BookingNotification.builder()
                .userId(100L)
                .title("Welcome")
                .message("Message")
                .type("GENERAL")
                .isRead(false)
                .delIf(false)
                .createdAt(Instant.now())
                .build());

    mockMvc
        .perform(get("/api/notifications/{id}", notification.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Welcome"));
  }

  @Test
  void getById_whenDeleted_returns404() throws Exception {
    BookingNotification notification =
        repository.save(
            BookingNotification.builder()
                .userId(100L)
                .title("Welcome")
                .message("Message")
                .type("GENERAL")
                .isRead(false)
                .delIf(true)
                .createdAt(Instant.now())
                .build());

    mockMvc
        .perform(get("/api/notifications/{id}", notification.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void create_whenValid_returns201() throws Exception {
    BookingNotificationRequest request =
        new BookingNotificationRequest(100L, "New Title", "New Message", "GENERAL", 200L);

    mockMvc
        .perform(
            post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("New Title"));
  }

  @Test
  void create_whenInvalidEnum_returns400() throws Exception {
    BookingNotificationRequest request =
        new BookingNotificationRequest(100L, "New Title", "New Message", "INVALID_TYPE", 200L);

    mockMvc
        .perform(
            post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_whenMissingRequired_returns400() throws Exception {
    BookingNotificationRequest request =
        new BookingNotificationRequest(null, "New Title", "New Message", "GENERAL", 200L);

    mockMvc
        .perform(
            post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_whenValid_returns200() throws Exception {
    BookingNotification notification =
        repository.save(
            BookingNotification.builder()
                .userId(100L)
                .title("Welcome")
                .message("Message")
                .type("GENERAL")
                .isRead(false)
                .delIf(false)
                .createdAt(Instant.now())
                .build());

    BookingNotificationRequest request =
        new BookingNotificationRequest(100L, "Updated Title", "Message", "GENERAL", 200L);

    mockMvc
        .perform(
            put("/api/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  @Test
  void update_whenDeleted_returns404() throws Exception {
    BookingNotification notification =
        repository.save(
            BookingNotification.builder()
                .userId(100L)
                .title("Welcome")
                .message("Message")
                .type("GENERAL")
                .isRead(false)
                .delIf(true)
                .createdAt(Instant.now())
                .build());

    BookingNotificationRequest request =
        new BookingNotificationRequest(100L, "Updated Title", "Message", "GENERAL", 200L);

    mockMvc
        .perform(
            put("/api/notifications/{id}", notification.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_whenExists_returns204() throws Exception {
    BookingNotification notification =
        repository.save(
            BookingNotification.builder()
                .userId(100L)
                .title("Welcome")
                .message("Message")
                .type("GENERAL")
                .isRead(false)
                .delIf(false)
                .createdAt(Instant.now())
                .build());

    mockMvc
        .perform(delete("/api/notifications/{id}", notification.getId()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/notifications/{id}", notification.getId()))
        .andExpect(status().isNotFound());
  }
}
