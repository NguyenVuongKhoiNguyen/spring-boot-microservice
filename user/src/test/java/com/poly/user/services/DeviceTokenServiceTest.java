package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.poly.user.dtos.responses.DeviceTokenResponse;
import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.mappers.DeviceTokenMapper;
import com.poly.user.models.DeviceToken;
import com.poly.user.models.User;
import com.poly.user.repositories.DeviceTokenRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

  @Mock private DeviceTokenRepository repository;
  @Mock private DeviceTokenMapper mapper;
  @InjectMocks private DeviceTokenService service;

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      User user = User.builder().id(100L).build();
      DeviceToken token =
          DeviceToken.builder().id(1L).user(user).token("token123").platform("IOS").build();
      DeviceTokenResponse response = new DeviceTokenResponse(1L, 100L, "token123", "IOS");

      Page<DeviceToken> page = new PageImpl<>(List.of(token));
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(mapper.toResponse(token)).thenReturn(response);

      PageResponse<DeviceTokenResponse> result =
          service.filterAndPaginate(
              null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmpty() {
      Page<DeviceToken> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<DeviceTokenResponse> result =
          service.filterAndPaginate(
              1L,
              100L,
              "token",
              "IOS",
              false,
              Instant.now(),
              Instant.now(),
              Instant.now(),
              Instant.now(),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).isEmpty();
    }
  }
}
