package com.poly.hotel.mappers;

import com.poly.hotel.dtos.requests.RoomTypeRequest;
import com.poly.hotel.dtos.responses.RoomTypeResponse;
import com.poly.hotel.models.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomTypeMapper {

  @Mapping(source = "hotel.id", target = "hotelId")
  RoomTypeResponse toResponse(RoomType entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  RoomType toEntity(RoomTypeRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  void updateEntity(RoomTypeRequest request, @MappingTarget RoomType entity);
}
