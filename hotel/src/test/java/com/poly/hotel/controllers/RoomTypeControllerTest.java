package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.RoomTypeRequest;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomTypeRepository;
import java.math.BigDecimal;
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
class RoomTypeControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private RoomTypeRepository repository;

  private Hotel hotel;

  @BeforeEach
  void setUp() {
    repository.deleteAllInBatch();
    hotelRepository.deleteAllInBatch();

    hotel =
        Hotel.builder()
            .name("Test Hotel")
            .description("Desc")
            .address("Address")
            .city("City")
            .country("Country")
            .phone("12345")
            .email("test@test.com")
            .starRating(5)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    hotel = hotelRepository.save(hotel);
  }

  @Test
  @DisplayName("POST /api/room-types - creates and returns 201")
  void createRoomType() throws Exception {
    RoomTypeRequest request =
        new RoomTypeRequest(
            hotel.getId(), "Deluxe", "Desc", 2, "King", BigDecimal.valueOf(100.0), true);

    mockMvc
        .perform(
            post("/api/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Deluxe"));
  }

  @Test
  @DisplayName("PUT /api/room-types/{id} - updates and returns 200")
  void updateRoomType() throws Exception {
    RoomType rt =
        RoomType.builder()
            .hotel(hotel)
            .name("Old")
            .description("Desc")
            .capacity(2)
            .bedType("King")
            .pricePerNight(BigDecimal.valueOf(100.0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    rt = repository.save(rt);

    RoomTypeRequest request =
        new RoomTypeRequest(
            hotel.getId(), "New", "Desc", 2, "King", BigDecimal.valueOf(100.0), true);

    mockMvc
        .perform(
            put("/api/room-types/{id}", rt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New"));
  }

  @Test
  @DisplayName("DELETE /api/room-types/{id} - soft deletes and returns 204")
  void deleteRoomType() throws Exception {
    RoomType rt =
        RoomType.builder()
            .hotel(hotel)
            .name("DeleteMe")
            .description("Desc")
            .capacity(2)
            .bedType("King")
            .pricePerNight(BigDecimal.valueOf(100.0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    rt = repository.save(rt);

    mockMvc.perform(delete("/api/room-types/{id}", rt.getId())).andExpect(status().isNoContent());

    RoomType found = repository.findById(rt.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
