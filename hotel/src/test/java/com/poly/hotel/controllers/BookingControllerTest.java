package com.poly.hotel.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.hotel.dtos.requests.BookingRequest;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.Room;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
import com.poly.hotel.repositories.RoomTypeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class BookingControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BookingRepository repository;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private RoomTypeRepository roomTypeRepository;
  @Autowired private RoomRepository roomRepository;
  @Autowired private BookingStatusHistoryRepository historyRepository;

  private Hotel hotel;
  private Room room;

  @BeforeEach
  void setUp() {
    historyRepository.deleteAllInBatch();
    repository.deleteAllInBatch();
    roomRepository.deleteAllInBatch();
    roomTypeRepository.deleteAllInBatch();
    hotelRepository.deleteAllInBatch();

    hotel =
        Hotel.builder()
            .name("H")
            .description("D")
            .address("A")
            .city("C")
            .country("C")
            .phone("1")
            .email("e@e")
            .starRating(5)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    hotel = hotelRepository.save(hotel);

    RoomType roomType =
        RoomType.builder()
            .hotel(hotel)
            .name("RT")
            .description("D")
            .capacity(2)
            .bedType("K")
            .pricePerNight(BigDecimal.TEN)
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    roomType = roomTypeRepository.save(roomType);

    room =
        Room.builder()
            .hotel(hotel)
            .roomType(roomType)
            .roomNumber("101")
            .status("AVAILABLE")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    room = roomRepository.save(room);
  }

  @Test
  @DisplayName("POST /api/bookings - creates and returns 201")
  void createBooking() throws Exception {
    BookingRequest request =
        new BookingRequest(
            1L,
            hotel.getId(),
            room.getId(),
            LocalDate.now(),
            LocalDate.now().plusDays(2),
            2,
            "PENDING",
            BigDecimal.valueOf(100.0),
            "req");

    mockMvc
        .perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("PUT /api/bookings/{id} - updates and returns 200")
  void updateBooking() throws Exception {
    Booking booking =
        Booking.builder()
            .userId(1L)
            .hotelId(hotel.getId())
            .roomId(room.getId())
            .checkInDate(LocalDate.now())
            .checkOutDate(LocalDate.now().plusDays(2))
            .guestCount(2)
            .totalPrice(BigDecimal.valueOf(100.0))
            .status("PENDING")
            .specialRequest("req")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    booking = repository.save(booking);

    BookingRequest request =
        new BookingRequest(
            1L,
            hotel.getId(),
            room.getId(),
            LocalDate.now(),
            LocalDate.now().plusDays(2),
            2,
            "CONFIRMED",
            BigDecimal.valueOf(100.0),
            "req");

    mockMvc
        .perform(
            put("/api/bookings/{id}", booking.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  @DisplayName("DELETE /api/bookings/{id} - soft deletes and returns 204")
  void deleteBooking() throws Exception {
    Booking booking =
        Booking.builder()
            .userId(1L)
            .hotelId(1L)
            .roomId(2L)
            .checkInDate(LocalDate.now())
            .checkOutDate(LocalDate.now().plusDays(2))
            .guestCount(2)
            .totalPrice(BigDecimal.valueOf(100.0))
            .status("PENDING")
            .specialRequest("req")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    booking = repository.save(booking);

    mockMvc
        .perform(delete("/api/bookings/{id}", booking.getId()))
        .andExpect(status().isNoContent());

    Booking found = repository.findById(booking.getId()).orElseThrow();
    assertThat(found.getDelIf()).isTrue();
  }
}
