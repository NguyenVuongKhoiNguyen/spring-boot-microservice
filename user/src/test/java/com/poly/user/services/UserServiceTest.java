package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.mappers.UserMapper;
import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
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
class UserServiceTest {

  @Mock private UserRepository repository;
  @Mock private UserMapper mapper;
  @InjectMocks private UserService service;

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      User user = User.builder().id(1L).email("test@example.com").build();
      UserResponse response = new UserResponse(1L, "test@example.com", "Test User", "123", true);

      Page<User> page = new PageImpl<>(List.of(user));
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(mapper.toResponse(user)).thenReturn(response);

      PageResponse<UserResponse> result =
          service.filterAndPaginate(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmpty() {
      Page<User> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<UserResponse> result =
          service.filterAndPaginate(
              1L,
              "a@a.com",
              "hash",
              "Name",
              "1",
              true,
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
