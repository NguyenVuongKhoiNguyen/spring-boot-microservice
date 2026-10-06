package com.poly.hotel.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.poly.hotel.models.Hotel;
import com.poly.hotel.models.Room;
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
class RoomRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private RoomRepository repository;
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

  private RoomType persistRoomType(Hotel hotel) {
    RoomType roomType =
        RoomType.builder()
            .hotel(hotel)
            .name("RT")
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

  private Room persistRoom(Hotel hotel, RoomType rt, String roomNumber) {
    Room room =
        Room.builder()
            .hotel(hotel)
            .roomType(rt)
            .roomNumber(roomNumber)
            .status("AVAILABLE")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    return em.persistAndFlush(room);
  }

  @Nested
  @DisplayName("CRUD")
  class Crud {
    @Test
    void save_whenValidEntity_persistsAndGeneratesId() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      Room room =
          Room.builder()
              .hotel(hotel)
              .roomType(rt)
              .roomNumber("101")
              .status("AVAILABLE")
              .active(true)
              .delIf(false)
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .build();

      Room saved = repository.save(room);
      em.flush();
      em.clear();

      assertThat(saved.getId()).isNotNull();
      Room found = em.find(Room.class, saved.getId());
      assertThat(found.getRoomNumber()).isEqualTo("101");
    }

    @Test
    void findById_whenExists_returnsEntity() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      Room room = persistRoom(hotel, rt, "FindMe");
      em.clear();

      Optional<Room> result = repository.findById(room.getId());
      assertThat(result).isPresent();
    }

    @Test
    void save_whenExistingEntity_updatesFields() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      Room room = persistRoom(hotel, rt, "UpdateMe");
      room.setRoomNumber("Updated");

      repository.save(room);
      em.flush();
      em.clear();

      Room found = em.find(Room.class, room.getId());
      assertThat(found.getRoomNumber()).isEqualTo("Updated");
    }

    @Test
    void deleteById_whenExists_removesRow() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      Room room = persistRoom(hotel, rt, "DeleteMe");
      em.clear();

      repository.deleteById(room.getId());
      em.flush();
      em.clear();

      Room found = em.find(Room.class, room.getId());
      assertThat(found).isNull();
    }
  }

  @Nested
  @DisplayName("Specification filters")
  class SpecificationFilters {
    @Test
    void findAll_whenNoFilters_returnsAllRows() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "101");
      persistRoom(hotel, rt, "102");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "Alpha");
      persistRoom(hotel, rt, "Beta");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, "lph", null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getRoomNumber()).isEqualTo("Alpha");
    }

    @Test
    void findAll_whenRelationIdGiven_returnsRowsOfThatParent() {
      Hotel hotel1 = persistHotel();
      Hotel hotel2 = persistHotel();
      RoomType rt1 = persistRoomType(hotel1);
      RoomType rt2 = persistRoomType(hotel2);
      persistRoom(hotel1, rt1, "101");
      persistRoom(hotel2, rt2, "102");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, hotel1.getId(), null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).getHotel().getId()).isEqualTo(hotel1.getId());
    }

    @Test
    void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "101");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null,
                  hotel.getId(),
                  rt.getId(),
                  "101",
                  "AVAILABLE",
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
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "101");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, "NoMatch", null, null, null, null, null, null, null),
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
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "A");
      persistRoom(hotel, rt, "B");
      persistRoom(hotel, rt, "C");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 2));

      assertThat(page.getContent()).hasSize(2);
      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getTotalPages()).isEqualTo(2);
      assertThat(page.isFirst()).isTrue();
    }

    @Test
    void findAll_whenSortGiven_ordersResults() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "Z");
      persistRoom(hotel, rt, "A");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "roomNumber")));

      assertThat(page.getContent().get(0).getRoomNumber()).isEqualTo("A");
      assertThat(page.getContent().get(1).getRoomNumber()).isEqualTo("Z");
    }

    @Test
    void findAll_whenPageBeyondLastPage_returnsEmptyContent() {
      Hotel hotel = persistHotel();
      RoomType rt = persistRoomType(hotel);
      persistRoom(hotel, rt, "A");

      Page<Room> page =
          repository.findAll(
              RoomSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
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
      RoomType rt = persistRoomType(hotel);
      Room room = persistRoom(hotel, rt, "101");
      room.setRoomNumber(null);

      assertThatThrownBy(() -> repository.saveAndFlush(room))
          .isInstanceOfAny(
              DataIntegrityViolationException.class,
              jakarta.validation.ConstraintViolationException.class);
    }
  }
}
