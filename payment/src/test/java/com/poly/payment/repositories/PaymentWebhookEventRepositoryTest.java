package com.poly.payment.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.poly.payment.models.PaymentWebhookEvent;
import com.poly.payment.models.WebhookResult;
import java.math.BigDecimal;
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
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("integration")
class PaymentWebhookEventRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("payment");

  @Autowired private PaymentWebhookEventRepository repository;
  @Autowired private TestEntityManager em;

  private PaymentWebhookEvent persistEvent(Long sepayTransactionId, WebhookResult result) {
    PaymentWebhookEvent event =
        PaymentWebhookEvent.builder()
            .sepayTransactionId(sepayTransactionId)
            .amount(new BigDecimal("1000"))
            .transferType("in")
            .result(result)
            .createdAt(Instant.now())
            .build();
    PaymentWebhookEvent saved = em.persistAndFlush(event);
    em.clear();
    return saved;
  }

  @Nested
  @DisplayName("Constraints and custom queries")
  class ConstraintsAndQueries {
    @Test
    void existsBySepayTransactionIdAndResultNot_whenExists_returnsTrue() {
      persistEvent(12345L, WebhookResult.MATCHED);

      boolean exists =
          repository.existsBySepayTransactionIdAndResultNot(12345L, WebhookResult.DUPLICATE);
      assertThat(exists).isTrue();
    }

    @Test
    void existsBySepayTransactionIdAndResultNot_whenOnlyDuplicateExists_returnsFalse() {
      persistEvent(12345L, WebhookResult.DUPLICATE);

      boolean exists =
          repository.existsBySepayTransactionIdAndResultNot(12345L, WebhookResult.DUPLICATE);
      assertThat(exists).isFalse();
    }

    @Test
    void existsBySepayTransactionIdAndResultNot_whenNotExists_returnsFalse() {
      boolean exists =
          repository.existsBySepayTransactionIdAndResultNot(99999L, WebhookResult.DUPLICATE);
      assertThat(exists).isFalse();
    }
  }
}
