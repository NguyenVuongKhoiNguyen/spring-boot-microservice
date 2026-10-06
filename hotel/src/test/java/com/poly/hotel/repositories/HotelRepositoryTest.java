package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.poly.hotel.models.Hotel;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Tag("integration")
class HotelRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private HotelRepository repository;
  @Autowired private TestEntityManager em;

  private Hotel persistHotel(String name) {
    Hotel hotel =
        Hotel.builder()
            .name(name)
            .description("Desc")
            .address("123 St")
            .city("City")
            .country("Country")
            .phone("123")
            .email("test@test.com")
            .starRating(5)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    return em.persistAndFlush(hotel);
  }

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
      Hotel hotel =
          Hotel.builder()
              .name("Valid")
              .address("Addr")
              .city("City")
              .country("Country")
              .checkInTime(LocalTime.of(14, 0))
              .checkOutTime(LocalTime.of(12, 0))
              .active(true)
              .delIf(false)
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();

      Hotel saved = repository.save(hotel);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      Hotel found = em.find(Hotel.class, saved.getId());
      assertThat(found.getName()).isEqualTo("Valid");
    }

    @Test
    void findById_whenExists_returnsEntity() {
      Hotel hotel = persistHotel("FindMe");
      em.clear();

      Optional<Hotel> result = repository.findById(hotel.getId());
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo("FindMe");
    }

    @Test
    void save_whenExistingEntity_updatesFields() {
      Hotel hotel = persistHotel("UpdateMe");
      hotel.setName("Updated");

      repository.save(hotel);
      em.flush();
      em.clear();

      Hotel found = em.find(Hotel.class, hotel.getId());
      assertThat(found.getName()).isEqualTo("Updated");
    }

    @Test
    void deleteById_whenExists_removesRow() {
      Hotel hotel = persistHotel("DeleteMe");
      em.clear();

      repository.deleteById(hotel.getId());
      em.flush();
      em.clear();

      Hotel found = em.find(Hotel.class, hotel.getId());
      assertThat(found).isNull();
    }
  }

  @Nested
  @DisplayName("Specification filters")
  class SpecificationFilters {
    @Test
    void findAll_whenNoFilters_returnsAllRows() {
      persistHotel("Hotel1");
      persistHotel("Hotel2");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() {
      persistHotel("Alpha");
      persistHotel("Beta");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, "lph", null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getName()).isEqualTo("Alpha");
    }

    @Test
    void findAll_whenRangeFilterGiven_returnsRowsInsideRange() {
      persistHotel("Hotel");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, null, null, null, null, null, null, null, 4, 6, null, null, null, null,
                  null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() {
      persistHotel("Hotel");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, "Hot", null, null, null, null, null, null, 5, 5, null, null, null, null,
                  true, false, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAll_whenNothingMatches_returnsEmptyPage() {
      persistHotel("Hotel");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, "NoMatch", null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Pagination and sorting")
  class PaginationAndSorting {
    @Test
    void findAll_whenPageRequested_returnsCorrectSliceAndTotals() {
      persistHotel("A");
      persistHotel("B");
      persistHotel("C");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(0, 2));

      assertThat(page.getContent()).hasSize(2);
      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getTotalPages()).isEqualTo(2);
      assertThat(page.isFirst()).isTrue();
    }

    @Test
    void findAll_whenSortGiven_ordersResults() {
      persistHotel("Z");
      persistHotel("A");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

      assertThat(page.getContent().get(0).getName()).isEqualTo("A");
      assertThat(page.getContent().get(1).getName()).isEqualTo("Z");
    }

    @Test
    void findAll_whenPageBeyondLastPage_returnsEmptyContent() {
      persistHotel("A");

      Page<Hotel> page =
          repository.findAll(
              HotelSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null, null, null, null, null, null),
              PageRequest.of(1, 10));

      assertThat(page.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Constraints and custom queries")
  class ConstraintsAndQueries {
    @Test
    void save_whenRequiredColumnIsNull_throwsException() {
      Hotel hotel = persistHotel("Test");
      hotel.setName(null);

      assertThatThrownBy(() -> repository.saveAndFlush(hotel))
          .isInstanceOfAny(
              DataIntegrityViolationException.class,
              jakarta.validation.ConstraintViolationException.class);
    }
  }
}
