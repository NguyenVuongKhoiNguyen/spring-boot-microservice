package com.poly.user.controllers;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserImageResponse;
import com.poly.user.services.UserImageService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/userimages")
@RequiredArgsConstructor
public class UserImageController {
  private final UserImageService service;

  @GetMapping
  public PageResponse<UserImageResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String imageUrl,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id, userId, imageUrl, delIf, createdAtFrom, createdAtTo, pageable);
  }
}
