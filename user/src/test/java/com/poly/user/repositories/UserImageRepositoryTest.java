package com.poly.user.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

import com.poly.user.models.User;
import com.poly.user.models.UserImage;
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
class UserImageRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private UserImageRepository repository;
  @Autowired private TestEntityManager em;

  private UserImage persistUserImage(String email, String url) {
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

    UserImage image =
        UserImage.builder().user(user).imageUrl(url).delIf(false).createdAt(Instant.now()).build();
    UserImage saved = em.persist(image);
    em.flush();
    return saved;
  }

  @Nested
  @DisplayName("findAll with Specification")
  class FindAllSpecification {

    @Test
    void shouldReturnAll_whenNoFilters() {
      persistUserImage("a@a.com", "url1");
      persistUserImage("b@b.com", "url2");

      Page<UserImage> result =
          repository.findAll(
              UserImageSpecification.filter(null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(2);
    }
  }
}
