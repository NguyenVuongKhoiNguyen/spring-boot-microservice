package com.poly.user.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceTokenRequest(
    @NotNull Long userId, @NotBlank String token, @NotBlank @Size(max = 20) String platform) {}
