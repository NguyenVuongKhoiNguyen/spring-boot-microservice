package com.poly.hotel.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.hotel.dtos.requests.BookingStatusHistoryRequest;
import com.poly.hotel.dtos.responses.BookingStatusHistoryResponse;
import com.poly.hotel.mappers.BookingStatusHistoryMapper;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.BookingStatusHistory;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingStatusHistoryServiceTest {

  @Mock private BookingStatusHistoryRepository repository;
  @Mock private BookingRepository bookingRepository;
  @Mock private BookingStatusHistoryMapper mapper;
  @InjectMocks private BookingStatusHistoryService service;

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void create_whenValidRequest_returnsResponse() {
      BookingStatusHistoryRequest request =
          new BookingStatusHistoryRequest(1L, "PENDING", "CONFIRMED");
      Booking booking = new Booking();
      booking.setDelIf(false);
      BookingStatusHistory entity = new BookingStatusHistory();
      BookingStatusHistory saved = new BookingStatusHistory();
      BookingStatusHistoryResponse response =
          new BookingStatusHistoryResponse(1L, 1L, "PENDING", "CONFIRMED", Instant.now());

      when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
      when(mapper.toEntity(request)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(saved);
      when(mapper.toResponse(saved)).thenReturn(response);

      BookingStatusHistoryResponse result = service.create(request);

      assertThat(result).isEqualTo(response);
      verify(repository).save(entity);
    }

    @Test
    void getById_whenExistsAndNotDeleted_returnsResponse() {
      BookingStatusHistory entity = new BookingStatusHistory();
      entity.setDelIf(false);
      BookingStatusHistoryResponse response =
          new BookingStatusHistoryResponse(1L, 1L, "PENDING", "CONFIRMED", Instant.now());

      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(mapper.toResponse(entity)).thenReturn(response);

      BookingStatusHistoryResponse result = service.getById(1L);

      assertThat(result).isEqualTo(response);
    }

    @Test
    void update_whenExistsAndNotDeleted_updatesAndReturns() {
      BookingStatusHistoryRequest request =
          new BookingStatusHistoryRequest(1L, "PENDING", "CANCELLED");
      Booking booking = new Booking();
      booking.setDelIf(false);
      BookingStatusHistory entity = new BookingStatusHistory();
      entity.setDelIf(false);
      BookingStatusHistory saved = new BookingStatusHistory();
      BookingStatusHistoryResponse response =
          new BookingStatusHistoryResponse(1L, 1L, "PENDING", "CANCELLED", Instant.now());

      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
      when(repository.save(entity)).thenReturn(saved);
      when(mapper.toResponse(saved)).thenReturn(response);

      BookingStatusHistoryResponse result = service.update(1L, request);

      assertThat(result).isEqualTo(response);
      verify(mapper).updateEntity(request, entity);
      verify(repository).save(entity);
    }

    @Test
    void delete_whenExistsAndNotDeleted_setsDelIfToTrueAndSaves() {
      BookingStatusHistory entity = new BookingStatusHistory();
      entity.setDelIf(false);

      when(repository.findById(1L)).thenReturn(Optional.of(entity));

      service.delete(1L);

      assertThat(entity.getDelIf()).isTrue();
      verify(repository).save(entity);
    }
  }
}
