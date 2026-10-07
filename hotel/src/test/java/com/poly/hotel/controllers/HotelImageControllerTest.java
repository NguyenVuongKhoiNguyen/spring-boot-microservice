package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.HotelImageRequest;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.HotelImage;
import com.poly.hotel.repositories.HotelImageRepository;
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
class HotelImageControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private HotelImageRepository repository;

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
  @DisplayName("POST /api/hotel-images - creates and returns 201")
  void createHotelImage() throws Exception {
    HotelImageRequest request = new HotelImageRequest(hotel.getId(), "url", true, 1);

    mockMvc
        .perform(
            post("/api/hotel-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.imageUrl").value("url"));
  }

  @Test
  @DisplayName("PUT /api/hotel-images/{id} - updates and returns 200")
  void updateHotelImage() throws Exception {
    HotelImage image =
        HotelImage.builder()
            .hotel(hotel)
            .imageUrl("Old")
            .isPrimary(true)
            .sortOrder(1)
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    image = repository.save(image);

    HotelImageRequest request = new HotelImageRequest(hotel.getId(), "New", true, 2);

    mockMvc
        .perform(
            put("/api/hotel-images/{id}", image.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imageUrl").value("New"));
  }

  @Test
  @DisplayName("DELETE /api/hotel-images/{id} - soft deletes and returns 204")
  void deleteHotelImage() throws Exception {
    HotelImage image =
        HotelImage.builder()
            .hotel(hotel)
            .imageUrl("DeleteMe")
            .isPrimary(true)
            .sortOrder(1)
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    image = repository.save(image);

    mockMvc
        .perform(delete("/api/hotel-images/{id}", image.getId()))
        .andExpect(status().isNoContent());

    HotelImage found = repository.findById(image.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
