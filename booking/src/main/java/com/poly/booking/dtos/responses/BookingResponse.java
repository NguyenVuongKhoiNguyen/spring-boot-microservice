package com.poly.booking.dtos.responses;

import com.poly.booking.models.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(
    Long id,
    Long userId,
    Long hotelId,
    Long roomId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    Integer guestCount,
    String status,
    BigDecimal totalPrice,
    String specialRequest) {
  public static BookingResponse from(Booking booking) {
    if (booking == null) {
      return null;
    }
    return new BookingResponse(
        booking.getId(),
        booking.getUserId(),
        booking.getHotelId(),
        booking.getRoomId(),
        booking.getCheckInDate(),
        booking.getCheckOutDate(),
        booking.getGuestCount(),
        booking.getStatus(),
        booking.getTotalPrice(),
        booking.getSpecialRequest());
  }
}
