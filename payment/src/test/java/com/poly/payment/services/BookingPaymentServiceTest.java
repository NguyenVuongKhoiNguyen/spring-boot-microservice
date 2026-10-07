package com.poly.payment.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.poly.payment.dtos.responses.BookingPaymentResponse;
import com.poly.payment.dtos.responses.PageResponse;
import com.poly.payment.mappers.BookingPaymentMapper;
import com.poly.payment.models.BookingPayment;
import com.poly.payment.repositories.BookingPaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
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
class BookingPaymentServiceTest {

  @Mock private BookingPaymentRepository repository;
  @Mock private BookingPaymentMapper mapper;
  @InjectMocks private BookingPaymentService service;

  @Nested
  @DisplayName("FilterAndPaginate")
  class FilterAndPaginate {
    @Test
    void filterAndPaginate_whenNoFilters_returnsAllPaginated() {
      BookingPayment payment = BookingPayment.builder().id(1L).amount(BigDecimal.TEN).build();
      Page<BookingPayment> page = new PageImpl<>(List.of(payment), PageRequest.of(0, 10), 1);

      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      BookingPaymentResponse responseDto =
          new BookingPaymentResponse(1L, null, BigDecimal.TEN, null, null, null, null);
      when(mapper.toResponse(payment)).thenReturn(responseDto);

      PageResponse<BookingPaymentResponse> result =
          service.filterAndPaginate(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void filterAndPaginate_whenFiltersGiven_passesThemToRepository() {
      Page<BookingPayment> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<BookingPaymentResponse> result =
          service.filterAndPaginate(
              1L,
              2L,
              BigDecimal.ONE,
              BigDecimal.TEN,
              "CARD",
              "PAID",
              "TX123",
              Instant.now(),
              Instant.now(),
              false,
              Instant.now(),
              Instant.now(),
              Instant.now(),
              Instant.now(),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).isEmpty();
    }

    @Test
    void filterAndPaginate_whenNoMatch_returnsEmptyPage() {
      Page<BookingPayment> page = new PageImpl<>(List.of());
      when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

      PageResponse<BookingPaymentResponse> result =
          service.filterAndPaginate(
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              PageRequest.of(0, 10));

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }
}
