package com.poly.user.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

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
class UserRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private UserRepository repository;
  @Autowired private TestEntityManager em;

  private User persistUser(String email) {
    User user =
        User.builder()
            .email(email)
            .passwordHash("hash")
            .fullName("Full Name")
            .phone("123")
            .active(true)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    User saved = em.persist(user);
    em.flush();
    return saved;
  }

  @Nested
  @DisplayName("findAll with Specification")
  class FindAllSpecification {

    @Test
    void shouldReturnAll_whenNoFilters() {
      persistUser("a@a.com");
      persistUser("b@b.com");

      Page<User> result =
          repository.findAll(
              UserSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldFilterByEmail() {
      persistUser("a@a.com");
      persistUser("b@b.com");

      Page<User> result =
          repository.findAll(
              UserSpecification.filter(
                  null, "a@a.com", null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getEmail()).isEqualTo("a@a.com");
    }
  }
}
