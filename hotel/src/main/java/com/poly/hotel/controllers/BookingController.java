package com.poly.hotel.controllers;

import com.poly.hotel.dtos.requests.BookingRequest;
import com.poly.hotel.dtos.responses.BookingResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.BookingService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final BookingService service;

  @GetMapping
  public PageResponse<BookingResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long hotelId,
      @RequestParam(required = false) Long roomId,
      @RequestParam(required = false) LocalDate checkInDateFrom,
      @RequestParam(required = false) LocalDate checkInDateTo,
      @RequestParam(required = false) LocalDate checkOutDateFrom,
      @RequestParam(required = false) LocalDate checkOutDateTo,
      @RequestParam(required = false) Integer guestCountFrom,
      @RequestParam(required = false) Integer guestCountTo,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) BigDecimal totalPriceFrom,
      @RequestParam(required = false) BigDecimal totalPriceTo,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
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
        createdAtFrom,
        createdAtTo,
        updatedAtFrom,
        updatedAtTo,
        pageable);
  }

  @GetMapping("/{id}")
  public BookingResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookingResponse create(@Valid @RequestBody BookingRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public BookingResponse update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
