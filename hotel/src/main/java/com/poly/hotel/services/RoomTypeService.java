package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomTypeResponse;
import com.poly.hotel.mappers.RoomTypeMapper;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.RoomTypeRepository;
import com.poly.hotel.repositories.RoomTypeSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
  private final RoomTypeRepository repository;
  private final RoomTypeMapper mapper;

  public PageResponse<RoomTypeResponse> filterAndPaginate(
      Long id,
      Long hotelId,
      String name,
      String description,
      Integer capacityFrom,
      Integer capacityTo,
      String bedType,
      BigDecimal pricePerNightFrom,
      BigDecimal pricePerNightTo,
      Boolean active,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<RoomType> page =
        repository.findAll(
            RoomTypeSpecification.filter(
                id,
                hotelId,
                name,
                description,
                capacityFrom,
                capacityTo,
                bedType,
                pricePerNightFrom,
                pricePerNightTo,
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
