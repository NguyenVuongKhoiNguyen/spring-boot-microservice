package com.poly.hotel.dtos.responses;

import com.poly.hotel.models.Hotel;
import java.time.LocalTime;

public record HotelResponse(
    Long id,
    String name,
    String description,
    String address,
    String city,
    String country,
    String phone,
    String email,
    Integer starRating,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    Boolean active) {
  public static HotelResponse from(Hotel hotel) {
    if (hotel == null) {
      return null;
    }
    return new HotelResponse(
        hotel.getId(),
        hotel.getName(),
        hotel.getDescription(),
        hotel.getAddress(),
        hotel.getCity(),
        hotel.getCountry(),
        hotel.getPhone(),
        hotel.getEmail(),
        hotel.getStarRating(),
        hotel.getCheckInTime(),
        hotel.getCheckOutTime(),
        hotel.getActive());
  }
}
