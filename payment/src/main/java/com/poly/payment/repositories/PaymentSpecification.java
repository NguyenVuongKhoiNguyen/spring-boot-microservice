package com.poly.payment.repositories;

import com.poly.payment.models.Payment;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentSpecification {
  private PaymentSpecification() {}

  public static Specification<Payment> filter(
      Long id,
      Long bookingId,
      BigDecimal amountFrom,
      BigDecimal amountTo,
      String paymentMethod,
      String status,
      String transactionId,
      Instant paidAtFrom,
      Instant paidAtTo,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (bookingId != null) predicates.add(cb.equal(root.get("bookingId"), bookingId));
      if (amountFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), amountFrom));
      if (amountTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("amount"), amountTo));
      if (paymentMethod != null && !paymentMethod.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("paymentMethod")), "%" + paymentMethod.toLowerCase() + "%"));
      if (status != null) predicates.add(cb.equal(root.get("status"), status));
      if (transactionId != null) predicates.add(cb.equal(root.get("transactionId"), transactionId));
      if (paidAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("paidAt"), paidAtFrom));
      if (paidAtTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("paidAt"), paidAtTo));
      if (delIf != null) predicates.add(cb.equal(root.get("delIf"), delIf));
      if (createdAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
      if (createdAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
      if (updatedAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtFrom));
      if (updatedAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedAtTo));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
