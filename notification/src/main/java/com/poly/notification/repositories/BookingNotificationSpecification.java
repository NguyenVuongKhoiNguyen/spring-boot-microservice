package com.poly.notification.repositories;

import com.poly.notification.models.BookingNotification;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BookingNotificationSpecification {
  private BookingNotificationSpecification() {}

  public static Specification<BookingNotification> filter(
      Long id,
      Long userId,
      String title,
      String message,
      String type,
      Long referenceId,
      Boolean isRead,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant readAtFrom,
      Instant readAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
      if (title != null && !title.isBlank())
        predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
      if (message != null && !message.isBlank())
        predicates.add(cb.like(cb.lower(root.get("message")), "%" + message.toLowerCase() + "%"));
      if (type != null && !type.isBlank())
        predicates.add(cb.like(cb.lower(root.get("type")), "%" + type.toLowerCase() + "%"));
      if (referenceId != null) predicates.add(cb.equal(root.get("referenceId"), referenceId));
      if (isRead != null) predicates.add(cb.equal(root.get("isRead"), isRead));
      if (delIf != null) predicates.add(cb.equal(root.get("delIf"), delIf));
      if (createdAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
      if (createdAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
      if (readAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("readAt"), readAtFrom));
      if (readAtTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("readAt"), readAtTo));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
