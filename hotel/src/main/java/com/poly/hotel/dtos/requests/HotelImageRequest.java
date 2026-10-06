package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HotelImageRequest(
    @NotNull Long hotelId,
    @NotBlank @Size(max = 1000) String imageUrl,
    @NotNull Boolean isPrimary,
    @NotNull Integer sortOrder) {}
