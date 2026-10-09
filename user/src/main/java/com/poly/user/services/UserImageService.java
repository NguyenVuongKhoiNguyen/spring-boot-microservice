package com.poly.user.services;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserImageResponse;
import com.poly.user.mappers.UserImageMapper;
import com.poly.user.models.UserImage;
import com.poly.user.repositories.UserImageRepository;
import com.poly.user.repositories.UserImageSpecification;
import com.poly.user.repositories.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserImageService {
  private final UserImageRepository repository;
  private final UserRepository userRepository;
  private final UserImageMapper mapper;

  public PageResponse<UserImageResponse> filterAndPaginate(
      Long id,
      Long userId,
      String imageUrl,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Pageable pageable) {
    Page<UserImage> page =
        repository.findAll(
            UserImageSpecification.filter(id, userId, imageUrl, delIf, createdAtFrom, createdAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  public UserImageResponse getById(Long id) {
    UserImage userImage =
        repository
            .findById(id)
            .filter(ui -> !ui.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException("UserImage not found"));
    return mapper.toResponse(userImage);
  }

  public UserImageResponse create(com.poly.user.dtos.requests.UserImageRequest request) {
    UserImage userImage = mapper.toEntity(request);
    com.poly.user.models.User user =
        userRepository
            .findById(request.userId())
            .filter(u -> !u.getDelIf())
            .orElseThrow(
                () -> new com.poly.user.exceptions.ResourceNotFoundException("User not found"));
    userImage.setUser(user);
    userImage.setDelIf(false);
    userImage.setCreatedAt(Instant.now());
    return mapper.toResponse(repository.save(userImage));
  }

  public UserImageResponse update(Long id, com.poly.user.dtos.requests.UserImageRequest request) {
    UserImage userImage =
        repository
            .findById(id)
            .filter(ui -> !ui.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException("UserImage not found"));
    mapper.updateEntity(request, userImage);
    com.poly.user.models.User user =
        userRepository
            .findById(request.userId())
            .filter(u -> !u.getDelIf())
            .orElseThrow(
                () -> new com.poly.user.exceptions.ResourceNotFoundException("User not found"));
    userImage.setUser(user);
    return mapper.toResponse(repository.save(userImage));
  }

  public void delete(Long id) {
    UserImage userImage =
        repository
            .findById(id)
            .filter(ui -> !ui.getDelIf())
            .orElseThrow(
                () ->
                    new com.poly.user.exceptions.ResourceNotFoundException("UserImage not found"));
    userImage.setDelIf(true);
    repository.save(userImage);
  }
}
