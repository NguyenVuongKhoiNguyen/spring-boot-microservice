package com.poly.hotel.services;

import com.poly.hotel.dtos.requests.RoomRequest;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomResponse;
import com.poly.hotel.exceptions.ResourceConflictException;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.mappers.RoomMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.Room;
import com.poly.hotel.models.RoomType;
import com.poly.hotel.repositories.BookingRepository;
import com.poly.hotel.repositories.HotelRepository;
import com.poly.hotel.repositories.RoomRepository;
import com.poly.hotel.repositories.RoomSpecification;
import com.poly.hotel.repositories.RoomTypeRepository;
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
public class RoomService {
  private final RoomRepository repository;
  private final HotelRepository hotelRepository;
  private final RoomTypeRepository roomTypeRepository;
  private final BookingRepository bookingRepository;
  private final RoomMapper mapper;

  @Transactional(readOnly = true)
  @Cacheable(value = "room-list", keyGenerator = "listKeyGenerator")
  public PageResponse<RoomResponse> filterAndPaginate(
      Long id,
      Long hotelId,
      Long roomTypeId,
      String roomNumber,
      String status,
      Boolean active,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Room> page =
        repository.findAll(
            RoomSpecification.filter(
                id,
                hotelId,
                roomTypeId,
                roomNumber,
                status,
                active,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "room-detail", key = "#id")
  public RoomResponse getById(Long id) {
    Room room =
        repository
            .findById(id)
            .filter(r -> !r.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    return mapper.toResponse(room);
  }

  @Transactional
  @CacheEvict(value = "room-list", allEntries = true)
  public RoomResponse create(RoomRequest request) {
    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    RoomType roomType =
        roomTypeRepository
            .findById(request.roomTypeId())
            .filter(rt -> !rt.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

    Room room = mapper.toEntity(request);
    room.setHotel(hotel);
    room.setRoomType(roomType);
    room.setDelIf(false);
    room.setCreatedAt(Instant.now());
    room.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(room));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "room-detail", key = "#id"),
        @CacheEvict(value = "room-list", allEntries = true)
      })
  public RoomResponse update(Long id, RoomRequest request) {
    Room room =
        repository
            .findById(id)
            .filter(r -> !r.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    Hotel hotel =
        hotelRepository
            .findById(request.hotelId())
            .filter(h -> !h.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

    RoomType roomType =
        roomTypeRepository
            .findById(request.roomTypeId())
            .filter(rt -> !rt.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType not found"));

    mapper.updateEntity(request, room);
    room.setHotel(hotel);
    room.setRoomType(roomType);
    room.setUpdatedAt(Instant.now());
    return mapper.toResponse(repository.save(room));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "room-detail", key = "#id"),
        @CacheEvict(value = "room-list", allEntries = true)
      })
  public void delete(Long id) {
    Room room =
        repository
            .findById(id)
            .filter(r -> !r.getDelIf())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    if (bookingRepository.existsByRoomIdAndDelIfFalse(id)) {
      throw new ResourceConflictException("Cannot delete Room with active bookings");
    }

    room.setDelIf(true);
    room.setUpdatedAt(Instant.now());
    repository.save(room);
  }
}
