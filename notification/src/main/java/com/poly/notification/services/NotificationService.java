package com.poly.notification.services;

import com.poly.notification.dtos.responses.NotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.mappers.NotificationMapper;
import com.poly.notification.models.Notification;
import com.poly.notification.repositories.NotificationRepository;
import com.poly.notification.repositories.NotificationSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository repository;
  private final NotificationMapper mapper;

  public PageResponse<NotificationResponse> filterAndPaginate(
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
    Page<Notification> page =
        repository.findAll(
            NotificationSpecification.filter(
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
