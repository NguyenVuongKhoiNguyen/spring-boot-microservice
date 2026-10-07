package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.poly.hotel.models.Booking;
import com.poly.hotel.models.BookingStatusHistory;
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
class BookingStatusHistoryRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private BookingStatusHistoryRepository repository;
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
      booking = em.persistAndFlush(booking);

      BookingStatusHistory history =
          BookingStatusHistory.builder()
              .booking(booking)
              .oldStatus("PENDING")
              .newStatus("CONFIRMED")
              .delIf(false)
              .changedAt(Instant.now())
              .build();

      BookingStatusHistory saved = repository.save(history);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      BookingStatusHistory found = em.find(BookingStatusHistory.class, saved.getId());
      assertThat(found.getNewStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void findById_whenExists_returnsEntity() {
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
      booking = em.persistAndFlush(booking);

      BookingStatusHistory history =
          BookingStatusHistory.builder()
              .booking(booking)
              .oldStatus("PENDING")
              .newStatus("CONFIRMED")
              .delIf(false)
              .changedAt(Instant.now())
              .build();
      em.persistAndFlush(history);
      em.clear();

      Optional<BookingStatusHistory> result = repository.findById(history.getId());
      assertThat(result).isPresent();
    }
  }
}
