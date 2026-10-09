package com.poly.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.poly.user.dtos.requests.UserRequest;
import com.poly.user.dtos.responses.PageResponse;
import com.poly.user.dtos.responses.UserResponse;
import com.poly.user.exceptions.ResourceNotFoundException;
import com.poly.user.models.User;
import com.poly.user.repositories.UserRepository;
import com.poly.user.repositories.UserRoleRepository;
import java.time.Instant;
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
class UserServiceCacheIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("user_test")
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

  @Autowired private UserService userService;
  @MockitoSpyBean private UserRepository userRepository;
  @Autowired private UserRoleRepository userRoleRepository;
  @Autowired private CacheManager cacheManager;

  private User savedUser;

  @BeforeEach
  void setUp() {
    userRoleRepository.deleteAll();
    userRepository.deleteAll();

    User user = new User();
    user.setEmail("test@test.com");
    user.setPasswordHash("hash");
    user.setFullName("Test User");
    user.setActive(true);
    user.setDelIf(false);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    savedUser = userRepository.save(user);
  }

  @Test
  void getById_cachesResult() throws Exception {
    clearInvocations(userRepository);
    // First call queries DB
    UserResponse response1 = userService.getById(savedUser.getId());
    for (int i = 0;
        i < 20 && cacheManager.getCache("user-profile").get(savedUser.getId()) == null;
        i++) {
      Thread.sleep(50);
    }
    assertThat(cacheManager.getCache("user-profile").get(savedUser.getId())).isNotNull();

    // Second call should come from cache
    UserResponse response2 = userService.getById(savedUser.getId());

    assertThat(response1).isEqualTo(response2);
    verify(userRepository, times(1)).findById(savedUser.getId());
  }

  @Test
  void update_evictsCache() {
    userService.getById(savedUser.getId()); // Caches it

    UserRequest updateReq = new UserRequest("test2@test.com", "hash2", "Updated User", "123");
    userService.update(savedUser.getId(), updateReq);

    // Should query DB again because cache was evicted
    clearInvocations(userRepository);
    UserResponse updated = userService.getById(savedUser.getId());
    assertThat(updated.fullName()).isEqualTo("Updated User");

    verify(userRepository, times(1)).findById(savedUser.getId()); // 1 for second get
  }

  @Test
  void delete_evictsCacheAndReturns404() {
    userService.getById(savedUser.getId()); // Caches it

    userService.delete(savedUser.getId());

    // Should query DB, find delIf=true, and throw
    assertThrows(ResourceNotFoundException.class, () -> userService.getById(savedUser.getId()));
  }

  @Test
  void filterAndPaginate_cachesResult() {
    clearInvocations(userRepository);
    PageResponse<UserResponse> page1 =
        userService.filterAndPaginate(
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
    PageResponse<UserResponse> page2 =
        userService.filterAndPaginate(
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
    verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
  }
}
