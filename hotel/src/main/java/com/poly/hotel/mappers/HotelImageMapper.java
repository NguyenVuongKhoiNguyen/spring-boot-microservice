package com.poly.hotel.mappers;

import com.poly.hotel.dtos.requests.HotelImageRequest;
import com.poly.hotel.dtos.responses.HotelImageResponse;
import com.poly.hotel.models.HotelImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HotelImageMapper {

  @Mapping(source = "hotel.id", target = "hotelId")
  HotelImageResponse toResponse(HotelImage entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  HotelImage toEntity(HotelImageRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  void updateEntity(HotelImageRequest request, @MappingTarget HotelImage entity);
}
