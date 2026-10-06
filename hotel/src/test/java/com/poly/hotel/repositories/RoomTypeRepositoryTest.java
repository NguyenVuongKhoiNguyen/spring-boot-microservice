package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.RoomType;
import java.math.BigDecimal;
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
class RoomTypeRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private RoomTypeRepository repository;
  @Autowired private TestEntityManager em;

  private Hotel persistHotel() {
    Hotel hotel =
        Hotel.builder()
            .name("Hotel")
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
    return em.persistAndFlush(hotel);
  }

  private RoomType persistRoomType(Hotel hotel, String name) {
    RoomType roomType =
        RoomType.builder()
            .hotel(hotel)
            .name(name)
            .description("Desc")
            .capacity(2)
            .bedType("King")
            .pricePerNight(BigDecimal.valueOf(100.0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    return em.persistAndFlush(roomType);
  }

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
      Hotel hotel = persistHotel();
      RoomType rt =
          RoomType.builder()
              .hotel(hotel)
              .name("Valid")
              .capacity(2)
              .bedType("King")
              .pricePerNight(BigDecimal.valueOf(100.0))
              .active(true)
              .delIf(false)
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();

      RoomType saved = repository.save(rt);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      RoomType found = em.find(RoomType.class, saved.getId());
      assertThat(found.getName()).isEqualTo("Valid");
    }

    @Test
    void findById_whenExists_returnsEntity() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel, "FindMe");
      em.clear();

      Optional<RoomType> result = repository.findById(rt.getId());
      assertThat(result).isPresent();
    }

    @Test
    void save_whenExistingEntity_updatesFields() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel, "UpdateMe");
      rt.setName("Updated");

      repository.save(rt);
      em.flush();
      em.clear();

      RoomType found = em.find(RoomType.class, rt.getId());
      assertThat(found.getName()).isEqualTo("Updated");
    }

    @Test
    void deleteById_whenExists_removesRow() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel, "DeleteMe");
      em.clear();

      repository.deleteById(rt.getId());
      em.flush();
      em.clear();

      RoomType found = em.find(RoomType.class, rt.getId());
      assertThat(found).isNull();
    }
  }

  @Nested
  @DisplayName("Specification filters")
  class SpecificationFilters {
    @Test
    void findAll_whenNoFilters_returnsAllRows() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "RT1");
      persistRoomType(hotel, "RT2");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "Alpha");
      persistRoomType(hotel, "Beta");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, "lph", null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getName()).isEqualTo("Alpha");
    }

    @Test
    void findAll_whenRangeFilterGiven_returnsRowsInsideRange() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "RT");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, null, null, 1, 3, null, null, null, null, null, null, null, null,
                  null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAll_whenRelationIdGiven_returnsRowsOfThatParent() {
      Hotel hotel1 = persistHotel();
      Hotel hotel2 = persistHotel();
      persistRoomType(hotel1, "RT1");
      persistRoomType(hotel2, "RT2");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null,
                  hotel1.getId(),
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
                  null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getHotel().getId()).isEqualTo(hotel1.getId());
    }

    @Test
    void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "RT");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null,
                  hotel.getId(),
                  "RT",
                  null,
                  2,
                  2,
                  "King",
                  null,
                  null,
                  true,
                  false,
                  null,
                  null,
                  null,
                  null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAll_whenNothingMatches_returnsEmptyPage() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "RT");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, "NoMatch", null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Pagination and sorting")
  class PaginationAndSorting {
    @Test
    void findAll_whenPageRequested_returnsCorrectSliceAndTotals() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "A");
      persistRoomType(hotel, "B");
      persistRoomType(hotel, "C");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(0, 2));

      assertThat(page.getContent()).hasSize(2);
      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getTotalPages()).isEqualTo(2);
      assertThat(page.isFirst()).isTrue();
    }

    @Test
    void findAll_whenSortGiven_ordersResults() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "Z");
      persistRoomType(hotel, "A");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

      assertThat(page.getContent().get(0).getName()).isEqualTo("A");
      assertThat(page.getContent().get(1).getName()).isEqualTo("Z");
    }

    @Test
    void findAll_whenPageBeyondLastPage_returnsEmptyContent() {
      Hotel hotel = persistHotel();
      persistRoomType(hotel, "A");

      Page<RoomType> page =
          repository.findAll(
              RoomTypeSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null, null, null,
                  null, null),
              PageRequest.of(1, 10));

      assertThat(page.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Constraints and custom queries")
  class ConstraintsAndQueries {
    @Test
    void save_whenRequiredColumnIsNull_throwsException() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel, "RT");
      rt.setName(null);

      assertThatThrownBy(() -> repository.saveAndFlush(rt))
          .isInstanceOfAny(
              DataIntegrityViolationException.class,
              jakarta.validation.ConstraintViolationException.class);
    }
  }
}
