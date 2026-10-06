package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.HotelImage;
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
class HotelImageRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private HotelImageRepository repository;
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

  private HotelImage persistHotelImage(Hotel hotel, String url) {
    HotelImage image =
        HotelImage.builder()
            .hotel(hotel)
            .imageUrl(url)
            .isPrimary(true)
            .sortOrder(1)
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    return em.persistAndFlush(image);
  }

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
      Hotel hotel = persistHotel();
      HotelImage image =
          HotelImage.builder()
              .hotel(hotel)
              .imageUrl("url")
              .isPrimary(true)
              .sortOrder(1)
              .delIf(false)
              .createdAt(Instant.now())
              .build();

      HotelImage saved = repository.save(image);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      HotelImage found = em.find(HotelImage.class, saved.getId());
      assertThat(found.getImageUrl()).isEqualTo("url");
    }

    @Test
    void findById_whenExists_returnsEntity() {
      Hotel hotel = persistHotel();
      HotelImage image = persistHotelImage(hotel, "url");
      em.clear();

      Optional<HotelImage> result = repository.findById(image.getId());
      assertThat(result).isPresent();
    }

    @Test
    void save_whenExistingEntity_updatesFields() {
      Hotel hotel = persistHotel();
      HotelImage image = persistHotelImage(hotel, "url1");
      image.setImageUrl("url2");

      repository.save(image);
      em.flush();
      em.clear();

      HotelImage found = em.find(HotelImage.class, image.getId());
      assertThat(found.getImageUrl()).isEqualTo("url2");
    }

    @Test
    void deleteById_whenExists_removesRow() {
      Hotel hotel = persistHotel();
      HotelImage image = persistHotelImage(hotel, "url");
      em.clear();

      repository.deleteById(image.getId());
      em.flush();
      em.clear();

      HotelImage found = em.find(HotelImage.class, image.getId());
      assertThat(found).isNull();
    }
  }

  @Nested
  @DisplayName("Specification filters")
  class SpecificationFilters {
    @Test
    void findAll_whenNoFilters_returnsAllRows() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "img1");
      persistHotelImage(hotel, "img2");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "Alpha");
      persistHotelImage(hotel, "Beta");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, "lph", null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getImageUrl()).isEqualTo("Alpha");
    }

    @Test
    void findAll_whenRelationIdGiven_returnsRowsOfThatParent() {
      Hotel hotel1 = persistHotel();
      Hotel hotel2 = persistHotel();
      persistHotelImage(hotel1, "img1");
      persistHotelImage(hotel2, "img2");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(
                  null, hotel1.getId(), null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getHotel().getId()).isEqualTo(hotel1.getId());
    }

    @Test
    void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "img1");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(
                  null, hotel.getId(), "img1", true, 1, false, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findAll_whenNothingMatches_returnsEmptyPage() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "img1");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, "NoMatch", null, null, null, null, null),
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
      persistHotelImage(hotel, "A");
      persistHotelImage(hotel, "B");
      persistHotelImage(hotel, "C");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, null, null, null, null, null, null),
              PageRequest.of(0, 2));

      assertThat(page.getContent()).hasSize(2);
      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getTotalPages()).isEqualTo(2);
      assertThat(page.isFirst()).isTrue();
    }

    @Test
    void findAll_whenSortGiven_ordersResults() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "Z");
      persistHotelImage(hotel, "A");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "imageUrl")));

      assertThat(page.getContent().get(0).getImageUrl()).isEqualTo("A");
      assertThat(page.getContent().get(1).getImageUrl()).isEqualTo("Z");
    }

    @Test
    void findAll_whenPageBeyondLastPage_returnsEmptyContent() {
      Hotel hotel = persistHotel();
      persistHotelImage(hotel, "A");

      Page<HotelImage> page =
          repository.findAll(
              HotelImageSpecification.filter(null, null, null, null, null, null, null, null),
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
      HotelImage image = persistHotelImage(hotel, "img1");
      image.setImageUrl(null);

      assertThatThrownBy(() -> repository.saveAndFlush(image))
          .isInstanceOfAny(
              DataIntegrityViolationException.class,
              jakarta.validation.ConstraintViolationException.class);
    }
  }
}
