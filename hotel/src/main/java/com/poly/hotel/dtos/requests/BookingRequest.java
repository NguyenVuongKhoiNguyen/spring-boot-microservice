package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingRequest(
    @NotNull Long userId,
    @NotNull Long hotelId,
    @NotNull Long roomId,
    @NotNull LocalDate checkInDate,
    @NotNull LocalDate checkOutDate,
    @NotNull @Positive Integer guestCount,
    String specialRequest,
    @NotNull BigDecimal totalPrice) {}
