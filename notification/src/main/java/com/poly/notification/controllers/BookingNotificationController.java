package com.poly.notification.controllers;

import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.services.BookingNotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class BookingNotificationController {
  private final BookingNotificationService service;

  @GetMapping
  public PageResponse<BookingNotificationResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String message,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long referenceId,
      @RequestParam(required = false) Boolean isRead,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant readAtFrom,
      @RequestParam(required = false) Instant readAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id,
        userId,
        title,
        message,
        type,
        referenceId,
        isRead,
        delIf,
        createdAtFrom,
        createdAtTo,
        readAtFrom,
        readAtTo,
        pageable);
  }
}
