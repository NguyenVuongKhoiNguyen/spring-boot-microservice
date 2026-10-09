package com.poly.hotel.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.poly.hotel.dtos.requests.HotelRequest;
import com.poly.hotel.dtos.responses.HotelResponse;
import com.poly.hotel.dtos.responses.PageResponse;
import com.poly.hotel.exceptions.ResourceNotFoundException;
import com.poly.hotel.models.Hotel;
import com.poly.hotel.repositories.HotelRepository;
import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = "spring.cache.type=redis")
@Testcontainers
@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HotelServiceCacheIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("hotel_test")
          .withUsername("test")
          .withPassword("test");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
          .withExposedPorts(6379)
          .withCommand("redis-server", "--requirepass", "testpass");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    registry.add("spring.data.redis.password", () -> "testpass");
  }

  @Autowired private HotelService hotelService;
  @MockitoSpyBean private HotelRepository hotelRepository;
  @Autowired private CacheManager cacheManager;

  private Hotel savedHotel;

  @BeforeEach
  void setUp() {
    hotelRepository.deleteAll();

    Hotel hotel =
        Hotel.builder()
            .name("Grand Luxury Hotel")
            .description("A grand luxury hotel")
            .address("123 Grand Ave")
            .city("Metropolis")
            .country("USA")
            .phone("123-456-7890")
            .email("info@grandhotel.com")
            .starRating(5)
            .checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(11, 0))
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    savedHotel = hotelRepository.save(hotel);
  }

  @Test
  void getById_cachesResult() throws Exception {
    clearInvocations(hotelRepository);
    // First call queries DB
    HotelResponse response1 = hotelService.getById(savedHotel.getId());
    for (int i = 0;
        i < 20 && cacheManager.getCache("hotel-detail").get(savedHotel.getId()) == null;
        i++) {
      Thread.sleep(50);
    }
    assertThat(cacheManager.getCache("hotel-detail").get(savedHotel.getId())).isNotNull();

    // Second call should come from cache
    HotelResponse response2 = hotelService.getById(savedHotel.getId());

    assertThat(response1).isEqualTo(response2);
    verify(hotelRepository, times(1)).findById(savedHotel.getId());
  }

  @Test
  void update_evictsCache() {
    hotelService.getById(savedHotel.getId()); // Caches it

    HotelRequest updateReq =
        new HotelRequest(
            "Updated Hotel Name",
            "Updated description",
            "456 Main St",
            "New City",
            "USA",
            "987-654-3210",
            "update@grandhotel.com",
            4,
            LocalTime.of(15, 0),
            LocalTime.of(12, 0),
            true);
    hotelService.update(savedHotel.getId(), updateReq);

    clearInvocations(hotelRepository);
    HotelResponse updated = hotelService.getById(savedHotel.getId());
    assertThat(updated.name()).isEqualTo("Updated Hotel Name");

    verify(hotelRepository, times(1)).findById(savedHotel.getId());
  }

  @Test
  void delete_evictsCacheAndReturns404() {
    hotelService.getById(savedHotel.getId()); // Caches it

    hotelService.delete(savedHotel.getId());

    assertThrows(ResourceNotFoundException.class, () -> hotelService.getById(savedHotel.getId()));
  }

  @Test
  void filterAndPaginate_cachesResult() {
    clearInvocations(hotelRepository);
    PageResponse<HotelResponse> page1 =
        hotelService.filterAndPaginate(
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
            null,
            null,
            null,
            null,
            null,
            PageRequest.of(0, 10));
    PageResponse<HotelResponse> page2 =
        hotelService.filterAndPaginate(
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
            null,
            null,
            null,
            null,
            null,
            PageRequest.of(0, 10));

    assertThat(page1).isEqualTo(page2);
    verify(hotelRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }
}
