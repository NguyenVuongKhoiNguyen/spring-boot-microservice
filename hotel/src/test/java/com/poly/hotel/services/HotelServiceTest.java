package com.poly.hotel.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.mappers.HotelMapper;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.repositories.HotelRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

  @Mock private HotelRepository repository;
  @Mock private HotelMapper mapper;
  @InjectMocks private HotelService service;

  @Nested
  @DisplayName("CRUD")
  class Crud {}

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {

    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      Pageable pageable = PageRequest.of(0, 10);
      Hotel hotel = new Hotel();
      HotelResponse response =
          new HotelResponse(1L, "Hotel", null, null, null, null, null, null, 5, null, null, true);
      Page<Hotel> page = new PageImpl<>(List.of(hotel), pageable, 1);

      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
      when(mapper.toResponse(hotel)).thenReturn(response);

      PageResponse<HotelResponse> result =
          service.filterAndPaginate(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null, null, null, null, pageable);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0)).isEqualTo(response);
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getTotalPages()).isEqualTo(1);
      assertThat(result.getPage()).isEqualTo(0);
      assertThat(result.getSize()).isEqualTo(10);
      assertThat(result.isFirst()).isTrue();
      assertThat(result.isLast()).isTrue();
    }

    @Test
    void filterAndPaginate_whenFiltersGiven_passesThemToRepository() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hotel> page = new PageImpl<>(List.of(), pageable, 0);
      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      PageResponse<HotelResponse> result =
          service.filterAndPaginate(
              1L, "Name", "Desc", "Addr", "City", "Country", "Phone", "Email", 1, 5, null, null,
              null, null, true, false, null, null, null, null, pageable);

      verify(repository).findAll(any(Specification.class), eq(pageable));
      assertThat(result.getContent()).isEmpty();
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmptyPage() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hotel> page = new PageImpl<>(List.of(), pageable, 0);
      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      PageResponse<HotelResponse> result =
          service.filterAndPaginate(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null, null, null, null, pageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Validation and business rules")
  class BusinessRules {}

  @Nested
  @DisplayName("Errors and edge cases")
  class ErrorsAndEdgeCases {}
}
