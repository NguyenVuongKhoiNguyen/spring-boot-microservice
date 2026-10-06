package com.poly.user.services;

import com.poly.user.dtos.responses.DeviceTokenResponse;
import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.mappers.DeviceTokenMapper;
import com.poly.user.models.DeviceToken;
import com.poly.user.repositories.DeviceTokenRepository;
import com.poly.user.repositories.DeviceTokenSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
  private final DeviceTokenRepository repository;
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
}
