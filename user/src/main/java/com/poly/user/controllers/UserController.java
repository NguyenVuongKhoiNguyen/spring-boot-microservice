package com.poly.user.controllers;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.services.UserService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService service;

  @GetMapping
  @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
  public PageResponse<UserResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String passwordHash,
      @RequestParam(required = false) String fullName,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id,
        email,
        passwordHash,
        fullName,
        phone,
        active,
        delIf,
        createdAtFrom,
        createdAtTo,
        updatedAtFrom,
        updatedAtTo,
        pageable);
  }

  @GetMapping("/me")
  public org.springframework.http.ResponseEntity<UserResponse> getMe(
      org.springframework.security.core.Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    return org.springframework.http.ResponseEntity.ok(service.getById(userId));
  }

  @GetMapping("/{id}")
  public org.springframework.http.ResponseEntity<UserResponse> getById(
      @org.springframework.web.bind.annotation.PathVariable Long id) {
    return org.springframework.http.ResponseEntity.ok(service.getById(id));
  }

  @org.springframework.web.bind.annotation.PostMapping
  public org.springframework.http.ResponseEntity<UserResponse> create(
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
          com.poly.user.dtos.requests.UserRequest request) {
    UserResponse response = service.create(request);
    return org.springframework.http.ResponseEntity.status(
            org.springframework.http.HttpStatus.CREATED)
        .body(response);
  }

  @org.springframework.web.bind.annotation.PutMapping("/{id}")
  public org.springframework.http.ResponseEntity<UserResponse> update(
      @org.springframework.web.bind.annotation.PathVariable Long id,
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody
          com.poly.user.dtos.requests.UserRequest request) {
    return org.springframework.http.ResponseEntity.ok(service.update(id, request));
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  public org.springframework.http.ResponseEntity<Void> delete(
      @org.springframework.web.bind.annotation.PathVariable Long id) {
    service.delete(id);
    return org.springframework.http.ResponseEntity.noContent().build();
  }
}
