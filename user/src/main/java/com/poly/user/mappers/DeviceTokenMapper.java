package com.poly.user.mappers;

import com.poly.user.dtos.requests.DeviceTokenRequest;
import com.poly.user.dtos.responses.DeviceTokenResponse;
import com.poly.user.models.DeviceToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceTokenMapper {

  @Mapping(source = "user.id", target = "userId")
  DeviceTokenResponse toResponse(DeviceToken entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "user", ignore = true)
  DeviceToken toEntity(DeviceTokenRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "user", ignore = true)
  void updateEntity(DeviceTokenRequest request, @MappingTarget DeviceToken entity);
}
