package com.poly.notification.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookingNotificationRequest(
    @NotNull Long userId,
    @NotBlank @Size(max = 255) String title,
    @NotBlank String message,
    @NotBlank
        @Pattern(
            regexp =
                "^(BOOKING_CONFIRMED|BOOKING_CANCELLED|CHECK_IN_REMINDER|CHECK_OUT_REMINDER|PAYMENT_SUCCESS|PAYMENT_FAILED|GENERAL)$")
        String type,
    Long referenceId) {}
