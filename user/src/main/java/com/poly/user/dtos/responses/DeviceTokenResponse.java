package com.poly.user.dtos.responses;

import com.poly.user.models.DeviceToken;

public record DeviceTokenResponse(Long id, Long userId, String token, String platform) {
  public static DeviceTokenResponse from(DeviceToken deviceToken) {
    if (deviceToken == null) {
      return null;
    }
    return new DeviceTokenResponse(
        deviceToken.getId(),
        deviceToken.getUser() != null ? deviceToken.getUser().getId() : null,
        deviceToken.getToken(),
        deviceToken.getPlatform());
  }
}
