package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.HotelImageResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.mappers.HotelImageMapper;
import com.poly.hotel.models.HotelImage;
import com.poly.hotel.repositories.HotelImageRepository;
import com.poly.hotel.repositories.HotelImageSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelImageService {
  private final HotelImageRepository repository;
  private final HotelImageMapper mapper;

  public PageResponse<HotelImageResponse> filterAndPaginate(
      Long id,
      Long hotelId,
      String imageUrl,
      Boolean isPrimary,
      Integer sortOrder,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Pageable pageable) {
    Page<HotelImage> page =
        repository.findAll(
            HotelImageSpecification.filter(
                id, hotelId, imageUrl, isPrimary, sortOrder, delIf, createdAtFrom, createdAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
