package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.HotelImageRequest;
import com.poly.hotel.dtos.responses.HotelImageResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.HotelImageMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.HotelImage;
import com.poly.hotel.repositories.HotelImageRepository;
import com.poly.hotel.repositories.HotelImageSpecification;
import com.poly.hotel.repositories.HotelRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelImageService {
  private final HotelImageRepository repository;
  private final HotelRepository hotelRepository;
  private final HotelImageMapper mapper;

  @Transactional(readOnly = true)
  @Cacheable(value = "hotelimage-list", keyGenerator = "listKeyGenerator")
  public PageResponse<HotelImageResponse> filterAndPaginate(
      Long id,
      Long hotelId,
      String imageUrl,
      Boolean isPrimary,
      Integer sortOrderFrom,
      Integer sortOrderTo,
      Instant createdAtFrom,
      Instant createdAtTo,
      Pageable pageable) {
    Page<HotelImage> page =
        repository.findAll(
            HotelImageSpecification.filter(
                id,
                hotelId,
                imageUrl,
                isPrimary,
                sortOrderFrom,
                sortOrderTo,
                createdAtFrom,
                createdAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "hotelimage-detail", key = "#id")
  public HotelImageResponse getById(Long id) {
    HotelImage image =
        repository
            .findById(id)
            .filter(i -> !i.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("HotelImage not found"));
    return mapper.toResponse(image);
  }

  @Transactional
  @CacheEvict(value = "hotelimage-list", allEntries = true)
  public HotelImageResponse create(HotelImageRequest request) {
    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    HotelImage image = mapper.toEntity(request);
    image.setHotel(hotel);
    image.setDelIf(false);
    image.setCreatedAt(Instant.now());
    return mapper.toResponse(repository.save(image));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "hotelimage-detail", key = "#id"),
        @CacheEvict(value = "hotelimage-list", allEntries = true)
      })
  public HotelImageResponse update(Long id, HotelImageRequest request) {
    HotelImage image =
        repository
            .findById(id)
            .filter(i -> !i.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("HotelImage not found"));

    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    mapper.updateEntity(request, image);
    image.setHotel(hotel);
    return mapper.toResponse(repository.save(image));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "hotelimage-detail", key = "#id"),
        @CacheEvict(value = "hotelimage-list", allEntries = true)
      })
  public void delete(Long id) {
    HotelImage image =
        repository
            .findById(id)
            .filter(i -> !i.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("HotelImage not found"));

    image.setDelIf(true);
    repository.save(image);
  }
}
