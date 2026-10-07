package com.poly.hotel.dtos.responses;

import com.poly.hotel.models.BookingStatusHistory;
import java.time.Instant;

public record BookingStatusHistoryResponse(
    Long id, Long bookingId, String oldStatus, String newStatus, Instant changedAt) {
  public static BookingStatusHistoryResponse from(BookingStatusHistory history) {
    if (history == null) {
      return null;
    }
    return new BookingStatusHistoryResponse(
        history.getId(),
        history.getBooking() != null ? history.getBooking().getId() : null,
        history.getOldStatus(),
        history.getNewStatus(),
        history.getChangedAt());
  }
}
