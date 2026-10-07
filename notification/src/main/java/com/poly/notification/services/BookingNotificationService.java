package com.poly.notification.services;

import com.poly.notification.dtos.requests.BookingNotificationRequest;
import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.dtos.responses.PageResponse;
import com.poly.notification.exceptions.ResourceNotFoundException;
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
                createdAtFrom,
                createdAtTo,
                readAtFrom,
                readAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  public BookingNotificationResponse getById(Long id) {
    BookingNotification entity =
        repository
            .findById(id)
            .filter(n -> !n.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingNotification not found"));
    return mapper.toResponse(entity);
  }

  public BookingNotificationResponse create(BookingNotificationRequest request) {
    BookingNotification entity = mapper.toEntity(request);
    entity.setDelIf(false);
    entity.setIsRead(false);
    entity = repository.save(entity);
    return mapper.toResponse(entity);
  }

  public BookingNotificationResponse update(Long id, BookingNotificationRequest request) {
    BookingNotification entity =
        repository
            .findById(id)
            .filter(n -> !n.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingNotification not found"));
    mapper.updateEntity(request, entity);
    entity = repository.save(entity);
    return mapper.toResponse(entity);
  }

  public void delete(Long id) {
    BookingNotification entity =
        repository
            .findById(id)
            .filter(n -> !n.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingNotification not found"));
    entity.setDelIf(true);
    repository.save(entity);
  }
}
