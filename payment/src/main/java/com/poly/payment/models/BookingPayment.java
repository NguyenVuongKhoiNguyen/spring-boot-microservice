package com.poly.payment.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPayment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "booking_id", nullable = false)
  private Long bookingId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "payment_method", length = 50)
  private String paymentMethod;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "transaction_id", length = 255)
  private String transactionId;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "payment_code", length = 50)
  private String paymentCode;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
