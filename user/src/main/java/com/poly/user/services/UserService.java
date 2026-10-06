package com.poly.user.services;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.mappers.UserMapper;
import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final UserMapper mapper;

  public PageResponse<UserResponse> filterAndPaginate(
      Long id,
      String email,
      String passwordHash,
      String fullName,
      String phone,
      String role,
      Boolean active,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<User> page =
        repository.findAll(
            UserSpecification.filter(
                id,
                email,
                passwordHash,
                fullName,
                phone,
                role,
                active,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
