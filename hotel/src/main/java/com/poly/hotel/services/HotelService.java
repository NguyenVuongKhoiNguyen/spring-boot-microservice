package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.mappers.HotelMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.HotelSpecification;
import java.time.Instant;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {
  private final HotelRepository repository;
  private final HotelMapper mapper;

  public PageResponse<HotelResponse> filterAndPaginate(
      Long id,
      String name,
      String description,
      String address,
      String city,
      String country,
      String phone,
      String email,
      Integer starRatingFrom,
      Integer starRatingTo,
      LocalTime checkInTimeFrom,
      LocalTime checkInTimeTo,
      LocalTime checkOutTimeFrom,
      LocalTime checkOutTimeTo,
      Boolean active,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Hotel> page =
        repository.findAll(
            HotelSpecification.filter(
                id,
                name,
                description,
                address,
                city,
                country,
                phone,
                email,
                starRatingFrom,
                starRatingTo,
                checkInTimeFrom,
                checkInTimeTo,
                checkOutTimeFrom,
                checkOutTimeTo,
                active,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
