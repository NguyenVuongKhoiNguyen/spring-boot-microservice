package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.RoomTypeRequest;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomTypeResponse;
import com.poly.hotel.exceptions.ResourceConflictException;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.RoomTypeMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
import com.poly.hotel.repositories.RoomTypeRepository;
import com.poly.hotel.repositories.RoomTypeSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
  private final RoomTypeRepository repository;
  private final HotelRepository hotelRepository;
  private final RoomRepository roomRepository;
  private final RoomTypeMapper mapper;

  @Transactional(readOnly = true)
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
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public RoomTypeResponse getById(Long id) {
    RoomType roomType =
        repository
            .findById(id)
            .filter(rt -> !rt.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));
    return mapper.toResponse(roomType);
  }

  @Transactional
  public RoomTypeResponse create(RoomTypeRequest request) {
    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    RoomType roomType = mapper.toEntity(request);
    roomType.setHotel(hotel);
    roomType.setDelIf(false);
    roomType.setCreatedAt(Instant.now());
    roomType.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(roomType));
  }

  @Transactional
  public RoomTypeResponse update(Long id, RoomTypeRequest request) {
    RoomType roomType =
        repository
            .findById(id)
            .filter(rt -> !rt.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    mapper.updateEntity(request, roomType);
    roomType.setHotel(hotel);
    roomType.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(roomType));
  }

  @Transactional
  public void delete(Long id) {
    RoomType roomType =
        repository
            .findById(id)
            .filter(rt -> !rt.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

    if (roomRepository.existsByRoomTypeIdAndDelIfFalse(id)) {
      throw new ResourceConflictException("Cannot delete RoomType with active rooms");
    }

    roomType.setDelIf(true);
    roomType.setUpdatedAt(Instant.now());
    repository.save(roomType);
  }
}
