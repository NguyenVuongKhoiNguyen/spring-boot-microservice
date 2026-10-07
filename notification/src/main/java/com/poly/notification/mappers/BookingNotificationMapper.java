package com.poly.notification.mappers;

import com.poly.notification.dtos.requests.BookingNotificationRequest;
import com.poly.notification.dtos.responses.BookingNotificationResponse;
import com.poly.notification.models.BookingNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingNotificationMapper {

  BookingNotificationResponse toResponse(BookingNotification entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  BookingNotification toEntity(BookingNotificationRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void updateEntity(BookingNotificationRequest request, @MappingTarget BookingNotification entity);
}
