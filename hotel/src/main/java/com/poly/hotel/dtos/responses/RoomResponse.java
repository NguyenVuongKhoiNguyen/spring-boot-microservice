package com.poly.hotel.dtos.responses;

import com.poly.hotel.models.Room;

public record RoomResponse(
    Long id, Long hotelId, Long roomTypeId, String roomNumber, String status, Boolean active) {
  public static RoomResponse from(Room room) {
    if (room == null) {
      return null;
    }
    return new RoomResponse(
        room.getId(),
        room.getHotel() != null ? room.getHotel().getId() : null,
        room.getRoomType() != null ? room.getRoomType().getId() : null,
        room.getRoomNumber(),
        room.getStatus(),
        room.getActive());
  }
}
