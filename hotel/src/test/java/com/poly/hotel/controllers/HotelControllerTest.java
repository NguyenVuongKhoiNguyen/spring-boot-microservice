package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.HotelRequest;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.repositories.HotelRepository;
import java.time.Instant;
import java.time.LocalTime;
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
class HotelControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private HotelRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAllInBatch();
  }

  @Test
  @DisplayName("POST /api/hotels - creates and returns 201")
  void createHotel() throws Exception {
    HotelRequest request =
        new HotelRequest(
            "Test Hotel",
            "Desc",
            "Address",
            "City",
            "Country",
            "12345",
            "test@test.com",
            5,
            LocalTime.of(14, 0),
            LocalTime.of(12, 0),
            true);

    mockMvc
        .perform(
            post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Test Hotel"));
  }

  @Test
  @DisplayName("PUT /api/hotels/{id} - updates and returns 200")
  void updateHotel() throws Exception {
    Hotel hotel =
        Hotel.builder()
            .name("Old")
            .description("Desc")
            .address("Addr")
            .city("City")
            .country("Country")
            .phone("12")
            .email("e@e.com")
            .starRating(3)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    hotel = repository.save(hotel);

    HotelRequest request =
        new HotelRequest(
            "New",
            "Desc",
            "Addr",
            "City",
            "Country",
            "12",
            "e@e.com",
            3,
            LocalTime.of(14, 0),
            LocalTime.of(12, 0),
            true);

    mockMvc
        .perform(
            put("/api/hotels/{id}", hotel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New"));
  }

  @Test
  @DisplayName("DELETE /api/hotels/{id} - soft deletes and returns 204")
  void deleteHotel() throws Exception {
    Hotel hotel =
        Hotel.builder()
            .name("DeleteMe")
            .description("Desc")
            .address("Addr")
            .city("City")
            .country("Country")
            .phone("12")
            .email("e@e.com")
            .starRating(3)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    hotel = repository.save(hotel);

    mockMvc.perform(delete("/api/hotels/{id}", hotel.getId())).andExpect(status().isNoContent());

    Hotel found = repository.findById(hotel.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
