package com.poly.user.dtos.responses;

import com.poly.user.models.User;

public record UserResponse(
    Long id, String email, String fullName, String phone, String role, Boolean active) {
  public static UserResponse from(User user) {
    if (user == null) {
      return null;
    }
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getPhone(),
        user.getRole(),
        user.getActive());
  }
}
