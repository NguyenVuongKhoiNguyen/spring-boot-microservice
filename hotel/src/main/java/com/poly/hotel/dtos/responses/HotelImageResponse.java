package com.poly.hotel.dtos.responses;

import com.poly.hotel.models.HotelImage;

public record HotelImageResponse(
    Long id, Long hotelId, String imageUrl, Boolean isPrimary, Integer sortOrder) {
  public static HotelImageResponse from(HotelImage hotelImage) {
    if (hotelImage == null) {
      return null;
    }
    return new HotelImageResponse(
        hotelImage.getId(),
        hotelImage.getHotel() != null ? hotelImage.getHotel().getId() : null,
        hotelImage.getImageUrl(),
        hotelImage.getIsPrimary(),
        hotelImage.getSortOrder());
  }
}
