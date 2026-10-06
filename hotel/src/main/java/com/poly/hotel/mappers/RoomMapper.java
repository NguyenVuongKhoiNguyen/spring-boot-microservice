package com.poly.hotel.mappers;

import com.poly.hotel.dtos.requests.RoomRequest;
import com.poly.hotel.dtos.responses.RoomResponse;
import com.poly.hotel.models.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomMapper {

  @Mapping(source = "hotel.id", target = "hotelId")
  @Mapping(source = "roomType.id", target = "roomTypeId")
  RoomResponse toResponse(Room entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  @Mapping(target = "roomType", ignore = true)
  Room toEntity(RoomRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "hotel", ignore = true)
  @Mapping(target = "roomType", ignore = true)
  void updateEntity(RoomRequest request, @MappingTarget Room entity);
}
