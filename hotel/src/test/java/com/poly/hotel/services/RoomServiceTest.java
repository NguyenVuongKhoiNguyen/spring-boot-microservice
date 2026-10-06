package com.poly.hotel.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.dtos.responses.RoomResponse;
import com.poly.hotel.mappers.RoomMapper;
import com.poly.hotel.models.Room;
import com.poly.hotel.repositories.RoomRepository;
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
class RoomServiceTest {

  @Mock private RoomRepository repository;
  @Mock private RoomMapper mapper;
  @InjectMocks private RoomService service;

  @Nested
  @DisplayName("CRUD")
  class Crud {}

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {

    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      Pageable pageable = PageRequest.of(0, 10);
      Room entity = new Room();
      RoomResponse response = new RoomResponse(1L, 1L, 1L, "101", "AVAILABLE", true);
      Page<Room> page = new PageImpl<>(List.of(entity), pageable, 1);

      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
      when(mapper.toResponse(entity)).thenReturn(response);

      PageResponse<RoomResponse> result =
          service.filterAndPaginate(
              null, null, null, null, null, null, null, null, null, null, null, pageable);

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
      Page<Room> page = new PageImpl<>(List.of(), pageable, 0);
      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      PageResponse<RoomResponse> result =
          service.filterAndPaginate(
              1L, 1L, 1L, "101", "AVAILABLE", true, false, null, null, null, null, pageable);

      verify(repository).findAll(any(Specification.class), eq(pageable));
      assertThat(result.getContent()).isEmpty();
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmptyPage() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Room> page = new PageImpl<>(List.of(), pageable, 0);
      when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

      PageResponse<RoomResponse> result =
          service.filterAndPaginate(
              null, null, null, null, null, null, null, null, null, null, null, pageable);

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
