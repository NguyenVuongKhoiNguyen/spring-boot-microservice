package com.poly.user.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

import com.poly.user.models.DeviceToken;
import com.poly.user.models.User;
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
@org.springframework.test.annotation.DirtiesContext(
    classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class DeviceTokenRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private DeviceTokenRepository repository;
  @Autowired private TestEntityManager em;

  private DeviceToken persistDeviceToken(String email, String tokenStr) {
    User user =
        User.builder()
            .email(email)
            .passwordHash("hash")
            .fullName("Full Name")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    em.persist(user);

    DeviceToken token =
        DeviceToken.builder()
            .user(user)
            .token(tokenStr)
            .platform("IOS")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    DeviceToken saved = em.persist(token);
    em.flush();
    return saved;
  }

  @Nested
  @DisplayName("findAll with Specification")
  class FindAllSpecification {

    @Test
    void shouldReturnAll_whenNoFilters() {
      persistDeviceToken("a@a.com", "token1");
      persistDeviceToken("b@b.com", "token2");

      Page<DeviceToken> result =
          repository.findAll(
              DeviceTokenSpecification.filter(null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(2);
    }
  }
}
