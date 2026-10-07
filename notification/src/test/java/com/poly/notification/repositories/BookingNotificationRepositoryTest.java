package com.poly.notification.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

import com.poly.notification.models.BookingNotification;
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
class BookingNotificationRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private BookingNotificationRepository repository;
  @Autowired private TestEntityManager em;

  private BookingNotification persistNotification(String title, String type) {
    BookingNotification notification =
        BookingNotification.builder()
            .userId(100L)
            .title(title)
            .message("Message for " + title)
            .type(type)
            .referenceId(200L)
            .isRead(false)
            .delIf(false)
            .createdAt(Instant.now())
            .build();
    BookingNotification saved = em.persist(notification);
    em.flush();
    return saved;
  }

  @Nested
  @DisplayName("findAll with Specification")
  class FindAllSpecification {

    @Test
    void shouldReturnAll_whenNoFilters() {
      persistNotification("Welcome", "INFO");
      persistNotification("Alert", "WARNING");

      Page<BookingNotification> result =
          repository.findAll(
              BookingNotificationSpecification.filter(
                  null, null, null, null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldFilterByTitle() {
      persistNotification("Welcome", "INFO");
      persistNotification("Alert", "WARNING");

      Page<BookingNotification> result =
          repository.findAll(
              BookingNotificationSpecification.filter(
                  null, null, "Welcome", null, null, null, null, null, null, null, null),
              PageRequest.of(0, 10));

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Welcome");
    }
  }
}
