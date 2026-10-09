package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.mappers.UserMapper;
import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
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
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
  @Mock private UserMapper mapper;
  @InjectMocks private UserService service;

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      User user = User.builder().id(1L).email("test@example.com").build();
      UserResponse response =
          new UserResponse(
              1L, "test@example.com", "Test User", "123", true, java.util.Collections.emptyList());

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

  @Test
  void getById_returnsUser() {
    User user = User.builder().id(1L).email("a@a.com").delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(mapper.toResponse(user))
        .thenReturn(new UserResponse(1L, "a@a.com", null, null, null, null));
    UserResponse result = service.getById(1L);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void create_savesAndReturnsUser() {
    com.poly.user.dtos.requests.UserRequest request =
        new com.poly.user.dtos.requests.UserRequest("a@a.com", "pw", "Name", "123");
    User user = User.builder().build();
    when(mapper.toEntity(request)).thenReturn(user);
    when(passwordEncoder.encode("pw")).thenReturn("hashed");
    when(repository.save(any(User.class))).thenReturn(user);
    when(mapper.toResponse(any(User.class)))
        .thenReturn(new UserResponse(1L, "a@a.com", null, null, null, null));

    UserResponse result = service.create(request);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void update_updatesAndReturnsUser() {
    com.poly.user.dtos.requests.UserRequest request =
        new com.poly.user.dtos.requests.UserRequest("a@a.com", "pw", "Name", "123");
    User user = User.builder().id(1L).delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.encode("pw")).thenReturn("hashed");
    when(repository.save(any(User.class))).thenReturn(user);
    when(mapper.toResponse(any(User.class)))
        .thenReturn(new UserResponse(1L, "a@a.com", null, null, null, null));

    UserResponse result = service.update(1L, request);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void delete_setsDelIfTrue() {
    User user = User.builder().id(1L).delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(user));
    service.delete(1L);
    assertThat(user.getDelIf()).isTrue();
  }
}
