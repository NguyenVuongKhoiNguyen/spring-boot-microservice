package com.poly.user.services;

import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserImageResponse;
import com.poly.user.mappers.UserImageMapper;
import com.poly.user.models.UserImage;
import com.poly.user.repositories.UserImageRepository;
import com.poly.user.repositories.UserImageSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserImageService {
  private final UserImageRepository repository;
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
}
