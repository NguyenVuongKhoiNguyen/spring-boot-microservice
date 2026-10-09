package com.poly.payment.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_webhook_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sepay_transaction_id")
  private Long sepayTransactionId;

  @Column(name = "payment_id")
  private Long paymentId;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "transfer_type", length = 10)
  private String transferType;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 20)
  private WebhookResult result;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
