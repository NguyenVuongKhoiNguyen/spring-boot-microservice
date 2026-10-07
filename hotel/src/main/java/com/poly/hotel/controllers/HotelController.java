package com.poly.hotel.controllers;

import com.poly.hotel.dtos.requests.HotelRequest;
import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.HotelService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
  private final HotelService service;

  @GetMapping
  public PageResponse<HotelResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String address,
      @RequestParam(required = false) String city,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Integer starRatingFrom,
      @RequestParam(required = false) Integer starRatingTo,
      @RequestParam(required = false) LocalTime checkInTimeFrom,
      @RequestParam(required = false) LocalTime checkInTimeTo,
      @RequestParam(required = false) LocalTime checkOutTimeFrom,
      @RequestParam(required = false) LocalTime checkOutTimeTo,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
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
        createdAtFrom,
        createdAtTo,
        updatedAtFrom,
        updatedAtTo,
        pageable);
  }

  @GetMapping("/{id}")
  public HotelResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HotelResponse create(@Valid @RequestBody HotelRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public HotelResponse update(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
