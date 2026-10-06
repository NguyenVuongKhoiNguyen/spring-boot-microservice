package com.poly.user.mappers;

import com.poly.user.dtos.requests.UserImageRequest;
import com.poly.user.dtos.responses.UserImageResponse;
import com.poly.user.models.UserImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserImageMapper {

  @Mapping(source = "user.id", target = "userId")
  UserImageResponse toResponse(UserImage entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "user", ignore = true)
  UserImage toEntity(UserImageRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "user", ignore = true)
  void updateEntity(UserImageRequest request, @MappingTarget UserImage entity);
}
