package com.poly.hotel.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.hotel.dtos.requests.BookingRequest;
import com.poly.hotel.dtos.responses.BookingResponse;
import com.poly.hotel.mappers.BookingMapper;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.Room;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository repository;
  @Mock private HotelRepository hotelRepository;
  @Mock private RoomRepository roomRepository;
  @Mock private BookingStatusHistoryRepository historyRepository;
  @Mock private BookingMapper mapper;
  @InjectMocks private BookingService service;

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void create_whenValidRequest_returnsResponse() {
      BookingRequest request =
          new BookingRequest(
              1L,
              1L,
              2L,
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              2,
              "PENDING",
              BigDecimal.TEN,
              "req");
      Booking entity = new Booking();
      Booking saved = new Booking();
      BookingResponse response =
          new BookingResponse(
              1L,
              1L,
              1L,
              2L,
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              2,
              "PENDING",
              BigDecimal.TEN,
              "req");

      Hotel hotel = new Hotel();
      hotel.setDelIf(false);
      Room room = new Room();
      room.setDelIf(false);
      when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
      when(roomRepository.findById(2L)).thenReturn(Optional.of(room));
      when(mapper.toEntity(request)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(saved);
      when(mapper.toResponse(saved)).thenReturn(response);

      BookingResponse result = service.create(request);

      assertThat(result).isEqualTo(response);
      verify(repository).save(entity);
    }

    @Test
    void getById_whenExistsAndNotDeleted_returnsResponse() {
      Booking entity = new Booking();
      entity.setDelIf(false);
      BookingResponse response =
          new BookingResponse(
              1L,
              1L,
              1L,
              2L,
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              2,
              "PENDING",
              BigDecimal.TEN,
              "req");

      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(mapper.toResponse(entity)).thenReturn(response);

      BookingResponse result = service.getById(1L);

      assertThat(result).isEqualTo(response);
    }

    @Test
    void update_whenExistsAndNotDeleted_updatesAndReturns() {
      BookingRequest request =
          new BookingRequest(
              1L,
              1L,
              2L,
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              2,
              "CONFIRMED",
              BigDecimal.TEN,
              "req");
      Booking entity = new Booking();
      entity.setDelIf(false);
      Booking saved = new Booking();
      BookingResponse response =
          new BookingResponse(
              1L,
              1L,
              1L,
              2L,
              LocalDate.now(),
              LocalDate.now().plusDays(1),
              2,
              "CONFIRMED",
              BigDecimal.TEN,
              "req");

      Hotel hotel = new Hotel();
      hotel.setDelIf(false);
      Room room = new Room();
      room.setDelIf(false);
      when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
      when(roomRepository.findById(2L)).thenReturn(Optional.of(room));
      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(repository.save(entity)).thenReturn(saved);
      when(mapper.toResponse(saved)).thenReturn(response);

      BookingResponse result = service.update(1L, request);

      assertThat(result).isEqualTo(response);
      verify(mapper).updateEntity(request, entity);
      verify(repository).save(entity);
    }

    @Test
    void delete_whenExistsAndNotDeleted_setsDelIfToTrueAndSaves() {
      Booking entity = new Booking();
      entity.setDelIf(false);

      when(repository.findById(1L)).thenReturn(Optional.of(entity));
      when(historyRepository.findByBookingIdAndDelIfFalse(1L)).thenReturn(Collections.emptyList());

      service.delete(1L);

      assertThat(entity.getDelIf()).isTrue();
      verify(repository).save(entity);
    }
  }
}
