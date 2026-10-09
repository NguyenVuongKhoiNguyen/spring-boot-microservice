package com.poly.user.dtos.responses;

import com.poly.user.models.User;
import java.util.Collections;
import java.util.List;

public record UserResponse(
    Long id, String email, String fullName, String phone, Boolean active, List<String> roles) {
  public static UserResponse from(User user) {
    return from(user, Collections.emptyList());
  }

  public static UserResponse from(User user, List<String> roles) {
    if (user == null) {
      return null;
    }
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getPhone(),
        user.getActive(),
        roles);
  }
}
