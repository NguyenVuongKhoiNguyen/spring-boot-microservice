package com.poly.booking.mappers;

import com.poly.booking.dtos.requests.BookingStatusHistoryRequest;
import com.poly.booking.dtos.responses.BookingStatusHistoryResponse;
import com.poly.booking.models.BookingStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingStatusHistoryMapper {

  @Mapping(source = "booking.id", target = "bookingId")
  BookingStatusHistoryResponse toResponse(BookingStatusHistory entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "changedAt", ignore = true)
  @Mapping(target = "booking", ignore = true)
  BookingStatusHistory toEntity(BookingStatusHistoryRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "changedAt", ignore = true)
  @Mapping(target = "booking", ignore = true)
  void updateEntity(
      BookingStatusHistoryRequest request, @MappingTarget BookingStatusHistory entity);
}
