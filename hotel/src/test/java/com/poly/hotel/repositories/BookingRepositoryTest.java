package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.poly.hotel.models.Booking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Tag("integration")
class BookingRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private BookingRepository repository;
  @Autowired private TestEntityManager em;

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
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

      Booking saved = repository.save(booking);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      Booking found = em.find(Booking.class, saved.getId());
      assertThat(found.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void findById_whenExists_returnsEntity() {
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
      em.persistAndFlush(booking);
      em.clear();

      Optional<Booking> result = repository.findById(booking.getId());
      assertThat(result).isPresent();
    }
  }
}
