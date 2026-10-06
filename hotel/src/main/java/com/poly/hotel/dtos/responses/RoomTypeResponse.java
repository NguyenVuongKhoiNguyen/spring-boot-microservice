package com.poly.hotel.dtos.responses;

import com.poly.hotel.models.RoomType;
import java.math.BigDecimal;

public record RoomTypeResponse(
    Long id,
    Long hotelId,
    String name,
    String description,
    Integer capacity,
    String bedType,
    BigDecimal pricePerNight,
    Boolean active) {
  public static RoomTypeResponse from(RoomType roomType) {
    if (roomType == null) {
      return null;
    }
    return new RoomTypeResponse(
        roomType.getId(),
        roomType.getHotel() != null ? roomType.getHotel().getId() : null,
        roomType.getName(),
        roomType.getDescription(),
        roomType.getCapacity(),
        roomType.getBedType(),
        roomType.getPricePerNight(),
        roomType.getActive());
  }
}
