package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.BookingResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.mappers.BookingMapper;
import com.poly.hotel.models.Booking;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository repository;
  private final BookingMapper mapper;

  public PageResponse<BookingResponse> filterAndPaginate(
      Long id,
      Long userId,
      Long hotelId,
      Long roomId,
      LocalDate checkInDateFrom,
      LocalDate checkInDateTo,
      LocalDate checkOutDateFrom,
      LocalDate checkOutDateTo,
      Integer guestCountFrom,
      Integer guestCountTo,
      String status,
      BigDecimal totalPriceFrom,
      BigDecimal totalPriceTo,
      String specialRequest,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Booking> page =
        repository.findAll(
            BookingSpecification.filter(
                id,
                userId,
                hotelId,
                roomId,
                checkInDateFrom,
                checkInDateTo,
                checkOutDateFrom,
                checkOutDateTo,
                guestCountFrom,
                guestCountTo,
                status,
                totalPriceFrom,
                totalPriceTo,
                specialRequest,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
