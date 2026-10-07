package com.poly.notification.services;

import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.mappers.BookingNotificationMapper;
import com.poly.notification.models.BookingNotification;
import com.poly.notification.repositories.BookingNotificationRepository;
import com.poly.notification.repositories.BookingNotificationSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingNotificationService {
  private final BookingNotificationRepository repository;
  private final BookingNotificationMapper mapper;

  public PageResponse<BookingNotificationResponse> filterAndPaginate(
      Long id,
      Long userId,
      String title,
      String message,
      String type,
      Long referenceId,
      Boolean isRead,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant readAtFrom,
      Instant readAtTo,
      Pageable pageable) {
    Page<BookingNotification> page =
        repository.findAll(
            BookingNotificationSpecification.filter(
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
                readAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
