package com.poly.user.dtos.responses;

import com.poly.user.models.UserImage;

public record UserImageResponse(Long id, Long userId, String imageUrl) {
  public static UserImageResponse from(UserImage userImage) {
    if (userImage == null) {
      return null;
    }
    return new UserImageResponse(
        userImage.getId(),
        userImage.getUser() != null ? userImage.getUser().getId() : null,
        userImage.getImageUrl());
  }
}
