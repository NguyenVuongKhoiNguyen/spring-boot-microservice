package com.poly.hotel.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomRequest(
    @NotNull Long hotelId,
    @NotNull Long roomTypeId,
    @NotBlank @Size(max = 50) String roomNumber,
    @NotBlank @Size(max = 50) String status) {}
