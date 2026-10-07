package com.poly.hotel.controllers;

import com.poly.hotel.dtos.requests.RoomTypeRequest;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomTypeResponse;
import com.poly.hotel.services.RoomTypeService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {
  private final RoomTypeService service;

  @GetMapping
  public PageResponse<RoomTypeResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long hotelId,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Integer capacityFrom,
      @RequestParam(required = false) Integer capacityTo,
      @RequestParam(required = false) String bedType,
      @RequestParam(required = false) BigDecimal pricePerNightFrom,
      @RequestParam(required = false) BigDecimal pricePerNightTo,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
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
        updatedAtTo,
        pageable);
  }

  @GetMapping("/{id}")
  public RoomTypeResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RoomTypeResponse create(@Valid @RequestBody RoomTypeRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public RoomTypeResponse update(
      @PathVariable Long id, @Valid @RequestBody RoomTypeRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
