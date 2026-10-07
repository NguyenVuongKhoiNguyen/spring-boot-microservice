package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.BookingRequest;
import com.poly.hotel.dtos.responses.BookingResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.BookingMapper;
import com.poly.hotel.models.Booking;
import com.poly.hotel.models.BookingStatusHistory;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.BookingSpecification;
import com.poly.hotel.repositories.BookingStatusHistoryRepository;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository repository;
  private final HotelRepository hotelRepository;
  private final RoomRepository roomRepository;
  private final BookingStatusHistoryRepository historyRepository;
  private final BookingMapper mapper;

  @Transactional(readOnly = true)
  public PageResponse<BookingResponse> filterAndPaginate(
      Long id,
      Long userId,
      Long hotelId,
      Long roomId,
      LocalDate checkInDateFrom,
      LocalDate checkInDateTo,
      LocalDate checkOutDateFrom,
      LocalDate checkOutDateTo,
      Integer guestCountFrom,
      Integer guestCountTo,
      String status,
      BigDecimal totalPriceFrom,
      BigDecimal totalPriceTo,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Booking> page =
        repository.findAll(
            BookingSpecification.filter(
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
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public BookingResponse getById(Long id) {
    Booking booking =
        repository
            .findById(id)
            .filter(b -> !b.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    return mapper.toResponse(booking);
  }

  @Transactional
  public BookingResponse create(BookingRequest request) {
    hotelRepository
        .findById(request.hotelId())
        .filter(h -> !h.getDelIf())
        .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    roomRepository
        .findById(request.roomId())
        .filter(r -> !r.getDelIf())
        .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    Booking booking = mapper.toEntity(request);
    booking.setDelIf(false);
    booking.setCreatedAt(Instant.now());
    booking.setUpdatedAt(Instant.now());
    booking = repository.save(booking);

    BookingStatusHistory history = new BookingStatusHistory();
    history.setBooking(booking);
    history.setOldStatus(null);
    history.setNewStatus(booking.getStatus());
    history.setDelIf(false);
    history.setChangedAt(Instant.now());
    historyRepository.save(history);

    return mapper.toResponse(booking);
  }

  @Transactional
  public BookingResponse update(Long id, BookingRequest request) {
    Booking booking =
        repository
            .findById(id)
            .filter(b -> !b.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    hotelRepository
        .findById(request.hotelId())
        .filter(h -> !h.getDelIf())
        .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    roomRepository
        .findById(request.roomId())
        .filter(r -> !r.getDelIf())
        .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    String oldStatus = booking.getStatus();
    mapper.updateEntity(request, booking);
    booking.setUpdatedAt(Instant.now());
    booking = repository.save(booking);

    if (oldStatus == null || !oldStatus.equals(booking.getStatus())) {
      BookingStatusHistory history = new BookingStatusHistory();
      history.setBooking(booking);
      history.setOldStatus(oldStatus);
      history.setNewStatus(booking.getStatus());
      history.setDelIf(false);
      history.setChangedAt(Instant.now());
      historyRepository.save(history);
    }

    return mapper.toResponse(booking);
  }

  @Transactional
  public void delete(Long id) {
    Booking booking =
        repository
            .findById(id)
            .filter(b -> !b.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    booking.setDelIf(true);
    booking.setUpdatedAt(Instant.now());
    repository.save(booking);

    List<BookingStatusHistory> histories = historyRepository.findByBookingIdAndDelIfFalse(id);
    histories.forEach(
        h -> {
          h.setDelIf(true);
          historyRepository.save(h);
        });
  }
}
