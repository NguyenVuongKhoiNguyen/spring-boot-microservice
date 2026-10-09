package com.poly.user.dtos.responses;

public record LoginResult(UserResponse user, String accessToken, String refreshToken) {}
