package com.poly.booking.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookingStatusHistoryRequest(
    @NotNull Long bookingId,
    @Size(max = 50) String oldStatus,
    @NotBlank @Size(max = 50) String newStatus) {}
