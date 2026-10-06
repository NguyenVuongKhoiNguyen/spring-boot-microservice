package com.poly.hotel.mappers;

import com.poly.hotel.dtos.requests.HotelRequest;
import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.models.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HotelMapper {

  HotelResponse toResponse(Hotel entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Hotel toEntity(HotelRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(HotelRequest request, @MappingTarget Hotel entity);
}
