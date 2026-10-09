package com.poly.user.services;

import com.poly.user.dtos.requests.UserRequest;
import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.exceptions.ResourceNotFoundException;
import com.poly.user.mappers.UserMapper;
import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import com.poly.user.repositories.UserSpecification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final UserRoleRepository userRoleRepository;
  private final UserMapper mapper;
  private final PasswordEncoder passwordEncoder;

  @Cacheable(value = "user-list", keyGenerator = "listKeyGenerator")
  public PageResponse<UserResponse> filterAndPaginate(
      Long id,
      String email,
      String passwordHash,
      String fullName,
      String phone,
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
                active,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Cacheable(value = "user-profile", key = "#id")
  public UserResponse getById(Long id) {
    User user =
        repository
            .findById(id)
            .filter(u -> !u.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<String> roles =
        userRoleRepository.findByUserIdAndDelIfFalse(user.getId()).stream()
            .map(ur -> ur.getRole().getName())
            .collect(Collectors.toList());

    UserResponse response = mapper.toResponse(user);
    return new UserResponse(
        response.id(),
        response.email(),
        response.fullName(),
        response.phone(),
        response.active(),
        roles);
  }

  @CacheEvict(value = "user-list", allEntries = true)
  public UserResponse create(UserRequest request) {
    User user = mapper.toEntity(request);
    user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
    user.setDelIf(false);
    user.setActive(true);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    User saved = repository.save(user);
    UserResponse response = mapper.toResponse(saved);
    return new UserResponse(
        response.id(),
        response.email(),
        response.fullName(),
        response.phone(),
        response.active(),
        new ArrayList<>());
  }

  @Caching(
      evict = {
        @CacheEvict(value = "user-profile", key = "#id"),
        @CacheEvict(value = "user-list", allEntries = true)
      })
  public UserResponse update(Long id, UserRequest request) {
    User user =
        repository
            .findById(id)
            .filter(u -> !u.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    mapper.updateEntity(request, user);
    user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
    user.setUpdatedAt(Instant.now());
    User saved = repository.save(user);

    List<String> roles =
        userRoleRepository.findByUserIdAndDelIfFalse(user.getId()).stream()
            .map(ur -> ur.getRole().getName())
            .collect(Collectors.toList());

    UserResponse response = mapper.toResponse(saved);
    return new UserResponse(
        response.id(),
        response.email(),
        response.fullName(),
        response.phone(),
        response.active(),
        roles);
  }

  @Caching(
      evict = {
        @CacheEvict(value = "user-profile", key = "#id"),
        @CacheEvict(value = "user-list", allEntries = true)
      })
  public void delete(Long id) {
    User user =
        repository
            .findById(id)
            .filter(u -> !u.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setDelIf(true);
    user.setUpdatedAt(Instant.now());
    repository.save(user);
  }
}
