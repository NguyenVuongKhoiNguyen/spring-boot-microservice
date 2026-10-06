package com.poly.notification.mappers;

import com.poly.notification.dtos.requests.NotificationRequest;
import com.poly.notification.dtos.responses.NotificationResponse;
import com.poly.notification.models.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

  NotificationResponse toResponse(Notification entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  Notification toEntity(NotificationRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void updateEntity(NotificationRequest request, @MappingTarget Notification entity);
}
