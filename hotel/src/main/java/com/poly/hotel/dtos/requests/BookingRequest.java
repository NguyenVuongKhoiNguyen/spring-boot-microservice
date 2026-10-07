package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingRequest(
    @NotNull Long userId,
    @NotNull Long hotelId,
    @NotNull Long roomId,
    @NotNull LocalDate checkInDate,
    @NotNull LocalDate checkOutDate,
    @NotNull @Min(1) Integer guestCount,
    @NotBlank @Pattern(regexp = "^(PENDING|CONFIRMED|CHECKED_IN|CHECKED_OUT|CANCELLED|COMPLETED)$")
        String status,
    @NotNull @Min(0) BigDecimal totalPrice,
    String specialRequest) {}
