package com.poly.user.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(max = 255) String email,
    @NotBlank @Size(max = 255) String passwordHash,
    @NotBlank @Size(max = 255) String fullName,
    @Size(max = 30) String phone) {}
