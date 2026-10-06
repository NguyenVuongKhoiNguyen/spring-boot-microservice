package com.poly.user.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserImageRequest(@NotNull Long userId, @NotBlank @Size(max = 1000) String imageUrl) {}
