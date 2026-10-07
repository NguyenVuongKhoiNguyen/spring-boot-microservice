package com.poly.hotel.mappers;

import com.poly.hotel.dtos.requests.BookingRequest;
import com.poly.hotel.dtos.responses.BookingResponse;
import com.poly.hotel.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

  BookingResponse toResponse(Booking entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Booking toEntity(BookingRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(BookingRequest request, @MappingTarget Booking entity);
}
