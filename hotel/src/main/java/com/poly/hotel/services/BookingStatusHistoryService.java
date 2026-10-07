package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.BookingStatusHistoryRequest;
import com.poly.hotel.dtos.responses.BookingStatusHistoryResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.BookingStatusHistoryMapper;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.BookingStatusHistory;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import com.poly.hotel.repositories.BookingStatusHistorySpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingStatusHistoryService {
  private final BookingStatusHistoryRepository repository;
  private final BookingRepository bookingRepository;
  private final BookingStatusHistoryMapper mapper;

  @Transactional(readOnly = true)
  public PageResponse<BookingStatusHistoryResponse> filterAndPaginate(
      Long id,
      Long bookingId,
      String oldStatus,
      String newStatus,
      Instant changedAtFrom,
      Instant changedAtTo,
      Pageable pageable) {
    Page<BookingStatusHistory> page =
        repository.findAll(
            BookingStatusHistorySpecification.filter(
                id, bookingId, oldStatus, newStatus, changedAtFrom, changedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public BookingStatusHistoryResponse getById(Long id) {
    BookingStatusHistory history =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingStatusHistory not found"));
    return mapper.toResponse(history);
  }

  @Transactional
  public BookingStatusHistoryResponse create(BookingStatusHistoryRequest request) {
    Booking booking =
        bookingRepository
            .findById(request.bookingId())
            .filter(b -> !b.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    BookingStatusHistory entity = mapper.toEntity(request);
    entity.setBooking(booking);
    entity.setDelIf(false);
    entity.setChangedAt(Instant.now());
    BookingStatusHistory saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public BookingStatusHistoryResponse update(Long id, BookingStatusHistoryRequest request) {
    BookingStatusHistory entity =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingStatusHistory not found"));
    Booking booking =
        bookingRepository
            .findById(request.bookingId())
            .filter(b -> !b.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    mapper.updateEntity(request, entity);
    entity.setBooking(booking);
    entity.setChangedAt(Instant.now());
    BookingStatusHistory saved = repository.save(entity);
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(Long id) {
    BookingStatusHistory entity =
        repository
            .findById(id)
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("BookingStatusHistory not found"));
    entity.setDelIf(true);
    repository.save(entity);
  }
}
