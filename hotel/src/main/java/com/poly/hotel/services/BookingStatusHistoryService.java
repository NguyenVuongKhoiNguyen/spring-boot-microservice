package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.BookingStatusHistoryResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.mappers.BookingStatusHistoryMapper;
import com.poly.hotel.models.BookingStatusHistory;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import com.poly.hotel.repositories.BookingStatusHistorySpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingStatusHistoryService {
  private final BookingStatusHistoryRepository repository;
  private final BookingStatusHistoryMapper mapper;

  public PageResponse<BookingStatusHistoryResponse> filterAndPaginate(
      Long id,
      Long bookingId,
      String oldStatus,
      String newStatus,
      Boolean delIf,
      Instant changedAtFrom,
      Instant changedAtTo,
      Pageable pageable) {
    Page<BookingStatusHistory> page =
        repository.findAll(
            BookingStatusHistorySpecification.filter(
                id, bookingId, oldStatus, newStatus, delIf, changedAtFrom, changedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
