package com.poly.notification.controllers;

import com.poly.notification.dtos.requests.BookingNotificationRequest;
import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.services.BookingNotificationService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
        createdAtFrom,
        createdAtTo,
        readAtFrom,
        readAtTo,
        pageable);
  }

  @GetMapping("/{id}")
  public BookingNotificationResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookingNotificationResponse create(
      @Valid @RequestBody BookingNotificationRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public BookingNotificationResponse update(
      @PathVariable Long id, @Valid @RequestBody BookingNotificationRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
