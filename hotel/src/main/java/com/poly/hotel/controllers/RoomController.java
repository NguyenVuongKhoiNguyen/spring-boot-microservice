package com.poly.hotel.controllers;

import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomResponse;
import com.poly.hotel.services.RoomService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
  private final RoomService service;

  @GetMapping
  public PageResponse<RoomResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long hotelId,
      @RequestParam(required = false) Long roomTypeId,
      @RequestParam(required = false) String roomNumber,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
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
        updatedAtTo,
        pageable);
  }
}
