package com.poly.hotel.controllers;

import com.poly.hotel.dtos.requests.BookingStatusHistoryRequest;
import com.poly.hotel.dtos.responses.BookingStatusHistoryResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.BookingStatusHistoryService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking-status-history")
@RequiredArgsConstructor
public class BookingStatusHistoryController {
  private final BookingStatusHistoryService service;

  @GetMapping
  public PageResponse<BookingStatusHistoryResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long bookingId,
      @RequestParam(required = false) String oldStatus,
      @RequestParam(required = false) String newStatus,
      @RequestParam(required = false) Instant changedAtFrom,
      @RequestParam(required = false) Instant changedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id, bookingId, oldStatus, newStatus, changedAtFrom, changedAtTo, pageable);
  }

  @GetMapping("/{id}")
  public BookingStatusHistoryResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookingStatusHistoryResponse create(
      @Valid @RequestBody BookingStatusHistoryRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public BookingStatusHistoryResponse update(
      @PathVariable Long id, @Valid @RequestBody BookingStatusHistoryRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
