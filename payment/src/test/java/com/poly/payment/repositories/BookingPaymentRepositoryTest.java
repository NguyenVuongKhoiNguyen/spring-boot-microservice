package com.poly.payment.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

import com.poly.payment.models.BookingPayment;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Testcontainers
class BookingPaymentRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private BookingPaymentRepository repository;
  @Autowired private TestEntityManager em;

  private BookingPayment persistPayment(BigDecimal amount, String method) {
    BookingPayment payment =
        BookingPayment.builder()
            .bookingId(1L)
            .amount(amount)
            .paymentMethod(method)
            .status("PAID")
            .transactionId("TX123")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    BookingPayment saved = em.persist(payment);
    em.flush();
    em.clear();
    return saved;
  }

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
      BookingPayment saved = persistPayment(BigDecimal.TEN, "CASH");
      assertThat(saved.getId()).isNotNull();

      BookingPayment found = repository.findById(saved.getId()).orElseThrow();
      assertThat(found.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void findById_whenExists_returnsEntity() {
      BookingPayment saved = persistPayment(BigDecimal.TEN, "CASH");
      assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void save_whenExistingEntity_updatesFields() {
      BookingPayment saved = persistPayment(BigDecimal.TEN, "CASH");
      BookingPayment found = repository.findById(saved.getId()).orElseThrow();
      found.setStatus("FAILED");
      repository.save(found);
      em.flush();
      em.clear();

      BookingPayment updated = repository.findById(saved.getId()).orElseThrow();
      assertThat(updated.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void deleteById_whenExists_removesRow() {
      BookingPayment saved = persistPayment(BigDecimal.TEN, "CASH");
      repository.deleteById(saved.getId());
      em.flush();
      em.clear();

      assertThat(repository.findById(saved.getId())).isEmpty();
    }
  }

  @Nested
  @DisplayName("Specification filters")
  class SpecificationFilters {
    @Test
    void findAll_whenNoFilters_returnsAllRows() {
      persistPayment(BigDecimal.TEN, "CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() {
      persistPayment(BigDecimal.TEN, "CREDIT CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null, null, null, null, "credit", null, null, null, null, null, null, null, null,
                  null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_whenRangeFilterGiven_returnsRowsInsideRange() {
      persistPayment(BigDecimal.TEN, "CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null,
                  null,
                  BigDecimal.ONE,
                  BigDecimal.valueOf(100),
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_whenRelationIdGiven_returnsRowsOfThatParent() {
      persistPayment(BigDecimal.TEN, "CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null, 1L, null, null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() {
      persistPayment(BigDecimal.TEN, "CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null,
                  1L,
                  BigDecimal.ONE,
                  BigDecimal.valueOf(100),
                  "card",
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_whenNothingMatches_returnsEmptyPage() {
      persistPayment(BigDecimal.TEN, "CARD");
      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null, 999L, null, null, null, null, null, null, null, null, null, null, null,
                  null),
              PageRequest.of(0, 10));
      assertThat(page.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Pagination and sorting")
  class PaginationAndSorting {
    @Test
    void findAll_whenPageRequested_returnsCorrectSliceAndTotals() {
      persistPayment(BigDecimal.TEN, "CASH");
      persistPayment(BigDecimal.ONE, "CARD");

      Page<BookingPayment> page =
          repository.findAll(
              BookingPaymentSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null),
              PageRequest.of(0, 1));

      assertThat(page.getTotalElements()).isEqualTo(2);
      assertThat(page.getTotalPages()).isEqualTo(2);
      assertThat(page.getContent()).hasSize(1);
    }
  }
}
