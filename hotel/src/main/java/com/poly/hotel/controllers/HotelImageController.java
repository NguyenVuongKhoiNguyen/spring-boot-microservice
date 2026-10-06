package com.poly.hotel.controllers;

import com.poly.hotel.dtos.responses.HotelImageResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.services.HotelImageService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotelimages")
@RequiredArgsConstructor
public class HotelImageController {
  private final HotelImageService service;

  @GetMapping
  public PageResponse<HotelImageResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long hotelId,
      @RequestParam(required = false) String imageUrl,
      @RequestParam(required = false) Boolean isPrimary,
      @RequestParam(required = false) Integer sortOrder,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
        id, hotelId, imageUrl, isPrimary, sortOrder, delIf, createdAtFrom, createdAtTo, pageable);
  }
}
