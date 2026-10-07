package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record HotelRequest(
    @NotBlank @Size(max = 255) String name,
    String description,
    @NotBlank @Size(max = 500) String address,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String country,
    @Size(max = 30) String phone,
    @Size(max = 255) String email,
    @Min(1) @Max(5) Integer starRating,
    @NotNull LocalTime checkInTime,
    @NotNull LocalTime checkOutTime,
    @NotNull Boolean active) {}
