package com.poly.notification.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
    @NotNull Long userId,
    @NotBlank @Size(max = 255) String title,
    @NotBlank String message,
    @NotBlank @Size(max = 50) String type,
    Long referenceId) {}
