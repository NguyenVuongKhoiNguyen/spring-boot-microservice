package com.poly.user.services;

import com.poly.user.dtos.responses.DeviceTokenResponse;
import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.mappers.DeviceTokenMapper;
import com.poly.user.models.DeviceToken;
import com.poly.user.repositories.DeviceTokenRepository;
import com.poly.user.repositories.DeviceTokenSpecification;
import com.poly.user.repositories.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
  private final DeviceTokenRepository repository;
  private final UserRepository userRepository;
  private final DeviceTokenMapper mapper;

  public PageResponse<DeviceTokenResponse> filterAndPaginate(
      Long id,
      Long userId,
      String token,
      String platform,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<DeviceToken> page =
        repository.findAll(
            DeviceTokenSpecification.filter(
                id,
                userId,
                token,
                platform,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  public DeviceTokenResponse getById(Long id) {
    DeviceToken deviceToken =
        repository
            .findById(id)
            .filter(dt -> !dt.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException(
                        "DeviceToken not found"));
    return mapper.toResponse(deviceToken);
  }

  public DeviceTokenResponse create(com.poly.user.dtos.requests.DeviceTokenRequest request) {
    DeviceToken deviceToken = mapper.toEntity(request);
    com.poly.user.models.User user =
        userRepository
            .findById(request.userId())
            .filter(u -> !u.getDelIf())
            .orElseThrow(
                () -> new com.poly.user.exceptions.ResourceNotFoundException("User not found"));
    deviceToken.setUser(user);
    deviceToken.setDelIf(false);
    deviceToken.setCreatedAt(Instant.now());
    deviceToken.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(deviceToken));
  }

  public DeviceTokenResponse update(
      Long id, com.poly.user.dtos.requests.DeviceTokenRequest request) {
    DeviceToken deviceToken =
        repository
            .findById(id)
            .filter(dt -> !dt.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException(
                        "DeviceToken not found"));
    mapper.updateEntity(request, deviceToken);
    com.poly.user.models.User user =
        userRepository
            .findById(request.userId())
            .filter(u -> !u.getDelIf())
            .orElseThrow(
                () -> new com.poly.user.exceptions.ResourceNotFoundException("User not found"));
    deviceToken.setUser(user);
    deviceToken.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(deviceToken));
  }

  public void delete(Long id) {
    DeviceToken deviceToken =
        repository
            .findById(id)
            .filter(dt -> !dt.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException(
                        "DeviceToken not found"));
    deviceToken.setDelIf(true);
    deviceToken.setUpdatedAt(Instant.now());
    repository.save(deviceToken);
  }
}
