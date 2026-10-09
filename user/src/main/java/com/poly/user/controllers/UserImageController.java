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

  @GetMapping("/{id}")
  public org.springframework.http.ResponseEntity<UserImageResponse> getById(
      @org.springframework.web.bind.annotation.PathVariable Long id) {
    return org.springframework.http.ResponseEntity.ok(service.getById(id));
  }

  @org.springframework.web.bind.annotation.PostMapping
  public org.springframework.http.ResponseEntity<UserImageResponse> create(
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
          com.poly.user.dtos.requests.UserImageRequest request) {
    UserImageResponse response = service.create(request);
    return org.springframework.http.ResponseEntity.status(
            org.springframework.http.HttpStatus.CREATED)
        .body(response);
  }

  @org.springframework.web.bind.annotation.PutMapping("/{id}")
  public org.springframework.http.ResponseEntity<UserImageResponse> update(
      @org.springframework.web.bind.annotation.PathVariable Long id,
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
          com.poly.user.dtos.requests.UserImageRequest request) {
    return org.springframework.http.ResponseEntity.ok(service.update(id, request));
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  public org.springframework.http.ResponseEntity<Void> delete(
      @org.springframework.web.bind.annotation.PathVariable Long id) {
    service.delete(id);
    return org.springframework.http.ResponseEntity.noContent().build();
  }
}
