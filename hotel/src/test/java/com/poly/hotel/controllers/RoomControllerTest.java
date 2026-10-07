package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.RoomRequest;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.Room;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
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
class RoomControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private RoomTypeRepository roomTypeRepository;
  @Autowired private RoomRepository repository;

  private Hotel hotel;
  private RoomType roomType;

  @BeforeEach
  void setUp() {
    repository.deleteAllInBatch();
    roomTypeRepository.deleteAllInBatch();
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

    roomType =
        RoomType.builder()
            .hotel(hotel)
            .name("RT")
            .description("Desc")
            .capacity(2)
            .bedType("King")
            .pricePerNight(BigDecimal.valueOf(100.0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    roomType = roomTypeRepository.save(roomType);
  }

  @Test
  @DisplayName("POST /api/rooms - creates and returns 201")
  void createRoom() throws Exception {
    RoomRequest request =
        new RoomRequest(hotel.getId(), roomType.getId(), "101", "AVAILABLE", true);

    mockMvc
        .perform(
            post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.roomNumber").value("101"));
  }

  @Test
  @DisplayName("PUT /api/rooms/{id} - updates and returns 200")
  void updateRoom() throws Exception {
    Room room =
        Room.builder()
            .hotel(hotel)
            .roomType(roomType)
            .roomNumber("Old")
            .status("AVAILABLE")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    room = repository.save(room);

    RoomRequest request =
        new RoomRequest(hotel.getId(), roomType.getId(), "New", "AVAILABLE", true);

    mockMvc
        .perform(
            put("/api/rooms/{id}", room.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomNumber").value("New"));
  }

  @Test
  @DisplayName("DELETE /api/rooms/{id} - soft deletes and returns 204")
  void deleteRoom() throws Exception {
    Room room =
        Room.builder()
            .hotel(hotel)
            .roomType(roomType)
            .roomNumber("DeleteMe")
            .status("AVAILABLE")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    room = repository.save(room);

    mockMvc.perform(delete("/api/rooms/{id}", room.getId())).andExpect(status().isNoContent());

    Room found = repository.findById(room.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
