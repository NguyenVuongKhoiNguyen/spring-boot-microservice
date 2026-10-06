package com.poly.hotel.services;

import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomResponse;
import com.poly.hotel.mappers.RoomMapper;
import com.poly.hotel.models.Room;
import com.poly.hotel.repositories.RoomRepository;
import com.poly.hotel.repositories.RoomSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
  private final RoomRepository repository;
  private final RoomMapper mapper;

  public PageResponse<RoomResponse> filterAndPaginate(
      Long id,
      Long hotelId,
      Long roomTypeId,
      String roomNumber,
      String status,
      Boolean active,
      Boolean delIf,
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
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
