package com.poly.hotel.controllers;

import com.poly.hotel.dtos.requests.HotelImageRequest;
import com.poly.hotel.dtos.responses.HotelImageResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.HotelImageService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel-images")
@RequiredArgsConstructor
public class HotelImageController {
  private final HotelImageService service;

  @GetMapping
  public PageResponse<HotelImageResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long hotelId,
      @RequestParam(required = false) String imageUrl,
      @RequestParam(required = false) Boolean isPrimary,
      @RequestParam(required = false) Integer sortOrderFrom,
      @RequestParam(required = false) Integer sortOrderTo,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id,
        hotelId,
        imageUrl,
        isPrimary,
        sortOrderFrom,
        sortOrderTo,
        createdAtFrom,
        createdAtTo,
        pageable);
  }

  @GetMapping("/{id}")
  public HotelImageResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HotelImageResponse create(@Valid @RequestBody HotelImageRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public HotelImageResponse update(
      @PathVariable Long id, @Valid @RequestBody HotelImageRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
