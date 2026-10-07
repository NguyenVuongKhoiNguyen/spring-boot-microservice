package com.poly.hotel.repositories;

import com.poly.hotel.models.BookingStatusHistory;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BookingStatusHistorySpecification {
  private BookingStatusHistorySpecification() {}

  public static Specification<BookingStatusHistory> filter(
      Long id,
      Long bookingId,
      String oldStatus,
      String newStatus,
      Instant changedAtFrom,
      Instant changedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("delIf"), false));
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (bookingId != null) predicates.add(cb.equal(root.get("booking").get("id"), bookingId));
      if (oldStatus != null && !oldStatus.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("oldStatus")), "%" + oldStatus.toLowerCase() + "%"));
      if (newStatus != null && !newStatus.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("newStatus")), "%" + newStatus.toLowerCase() + "%"));
      if (changedAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("changedAt"), changedAtFrom));
      if (changedAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("changedAt"), changedAtTo));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
