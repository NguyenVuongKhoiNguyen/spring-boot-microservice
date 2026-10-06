package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RoomTypeRequest(
    @NotNull Long hotelId,
    @NotBlank @Size(max = 100) String name,
    String description,
    @NotNull Integer capacity,
    @Size(max = 100) String bedType,
    @NotNull BigDecimal pricePerNight) {}
