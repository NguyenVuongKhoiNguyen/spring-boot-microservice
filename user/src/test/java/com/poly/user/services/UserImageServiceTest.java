package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserImageResponse;
import com.poly.user.mappers.UserImageMapper;
import com.poly.user.models.User;
import com.poly.user.models.UserImage;
import com.poly.user.repositories.UserImageRepository;
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
class UserImageServiceTest {

  @Mock private UserImageRepository repository;
  @Mock private com.poly.user.repositories.UserRepository userRepository;
  @Mock private UserImageMapper mapper;
  @InjectMocks private UserImageService service;

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      User user = User.builder().id(100L).build();
      UserImage image = UserImage.builder().id(1L).user(user).imageUrl("url").build();
      UserImageResponse response = new UserImageResponse(1L, 100L, "url");

      Page<UserImage> page = new PageImpl<>(List.of(image));
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
      when(mapper.toResponse(image)).thenReturn(response);

      PageResponse<UserImageResponse> result =
          service.filterAndPaginate(null, null, null, null, null, null, PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmpty() {
      Page<UserImage> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<UserImageResponse> result =
          service.filterAndPaginate(
              1L, 100L, "url", false, Instant.now(), Instant.now(), PageRequest.of(0, 10));

      assertThat(result.getContent()).isEmpty();
    }
  }

  @Test
  void getById_returnsUserImage() {
    UserImage userImage = UserImage.builder().id(1L).delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(userImage));
    when(mapper.toResponse(userImage)).thenReturn(new UserImageResponse(1L, 100L, "url"));
    UserImageResponse result = service.getById(1L);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void create_savesAndReturnsUserImage() {
    com.poly.user.dtos.requests.UserImageRequest request =
        new com.poly.user.dtos.requests.UserImageRequest(100L, "url");
    UserImage userImage = UserImage.builder().build();
    User user = User.builder().id(100L).delIf(false).build();
    when(mapper.toEntity(request)).thenReturn(userImage);
    when(userRepository.findById(100L)).thenReturn(java.util.Optional.of(user));
    when(repository.save(any(UserImage.class))).thenReturn(userImage);
    when(mapper.toResponse(any(UserImage.class)))
        .thenReturn(new UserImageResponse(1L, 100L, "url"));

    UserImageResponse result = service.create(request);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void update_updatesAndReturnsUserImage() {
    com.poly.user.dtos.requests.UserImageRequest request =
        new com.poly.user.dtos.requests.UserImageRequest(100L, "url");
    UserImage userImage = UserImage.builder().id(1L).delIf(false).build();
    User user = User.builder().id(100L).delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(userImage));
    when(userRepository.findById(100L)).thenReturn(java.util.Optional.of(user));
    when(repository.save(any(UserImage.class))).thenReturn(userImage);
    when(mapper.toResponse(any(UserImage.class)))
        .thenReturn(new UserImageResponse(1L, 100L, "url"));

    UserImageResponse result = service.update(1L, request);
    assertThat(result.id()).isEqualTo(1L);
  }

  @Test
  void delete_setsDelIfTrue() {
    UserImage userImage = UserImage.builder().id(1L).delIf(false).build();
    when(repository.findById(1L)).thenReturn(java.util.Optional.of(userImage));
    service.delete(1L);
    assertThat(userImage.getDelIf()).isTrue();
  }
}
