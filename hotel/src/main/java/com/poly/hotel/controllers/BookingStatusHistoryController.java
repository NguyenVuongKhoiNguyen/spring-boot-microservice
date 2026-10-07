package com.poly.hotel.controllers;

import com.poly.hotel.dtos.responses.BookingStatusHistoryResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.BookingStatusHistoryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookingstatushistorys")
@RequiredArgsConstructor
public class BookingStatusHistoryController {
  private final BookingStatusHistoryService service;

  @GetMapping
  public PageResponse<BookingStatusHistoryResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long bookingId,
      @RequestParam(required = false) String oldStatus,
      @RequestParam(required = false) String newStatus,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant changedAtFrom,
      @RequestParam(required = false) Instant changedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id, bookingId, oldStatus, newStatus, delIf, changedAtFrom, changedAtTo, pageable);
  }
}
