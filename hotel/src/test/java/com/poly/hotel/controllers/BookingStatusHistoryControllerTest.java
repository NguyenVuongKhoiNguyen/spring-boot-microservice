package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.BookingStatusHistoryRequest;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.BookingStatusHistory;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
class BookingStatusHistoryControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BookingStatusHistoryRepository repository;
  @Autowired private BookingRepository bookingRepository;

  @BeforeEach
  void setUp() {
    repository.deleteAllInBatch();
    bookingRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("POST /api/booking-status-history - creates and returns 201")
  void createHistory() throws Exception {
    Booking booking =
        Booking.builder()
            .userId(1L)
            .hotelId(1L)
            .roomId(1L)
            .checkInDate(LocalDate.now())
            .checkOutDate(LocalDate.now().plusDays(1))
            .guestCount(2)
            .status("PENDING")
            .totalPrice(BigDecimal.TEN)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    booking = bookingRepository.save(booking);

    BookingStatusHistoryRequest request =
        new BookingStatusHistoryRequest(booking.getId(), "PENDING", "CONFIRMED");

    mockMvc
        .perform(
            post("/api/booking-status-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.newStatus").value("CONFIRMED"));
  }

  @Test
  @DisplayName("PUT /api/booking-status-history/{id} - updates and returns 200")
  void updateHistory() throws Exception {
    Booking booking =
        Booking.builder()
            .userId(1L)
            .hotelId(1L)
            .roomId(1L)
            .checkInDate(LocalDate.now())
            .checkOutDate(LocalDate.now().plusDays(1))
            .guestCount(2)
            .status("PENDING")
            .totalPrice(BigDecimal.TEN)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    booking = bookingRepository.save(booking);

    BookingStatusHistory history =
        BookingStatusHistory.builder()
            .booking(booking)
            .oldStatus("PENDING")
            .newStatus("CONFIRMED")
            .delIf(false)
            .changedAt(Instant.now())
            .build();
    history = repository.save(history);

    BookingStatusHistoryRequest request =
        new BookingStatusHistoryRequest(booking.getId(), "PENDING", "CANCELLED");

    mockMvc
        .perform(
            put("/api/booking-status-history/{id}", history.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.newStatus").value("CANCELLED"));
  }

  @Test
  @DisplayName("DELETE /api/booking-status-history/{id} - soft deletes and returns 204")
  void deleteHistory() throws Exception {
    Booking booking =
        Booking.builder()
            .userId(1L)
            .hotelId(1L)
            .roomId(1L)
            .checkInDate(LocalDate.now())
            .checkOutDate(LocalDate.now().plusDays(1))
            .guestCount(2)
            .status("PENDING")
            .totalPrice(BigDecimal.TEN)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    booking = bookingRepository.save(booking);

    BookingStatusHistory history =
        BookingStatusHistory.builder()
            .booking(booking)
            .oldStatus("PENDING")
            .newStatus("CONFIRMED")
            .delIf(false)
            .changedAt(Instant.now())
            .build();
    history = repository.save(history);

    mockMvc
        .perform(delete("/api/booking-status-history/{id}", history.getId()))
        .andExpect(status().isNoContent());

    BookingStatusHistory found = repository.findById(history.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
