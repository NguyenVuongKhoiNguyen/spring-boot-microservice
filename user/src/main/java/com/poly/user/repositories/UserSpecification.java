package com.poly.user.repositories;

import com.poly.user.models.User;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {
  private UserSpecification() {}

  public static Specification<User> filter(
      Long id,
      String email,
      String passwordHash,
      String fullName,
      String phone,
      Boolean active,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (email != null && !email.isBlank())
        predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
      if (passwordHash != null && !passwordHash.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("passwordHash")), "%" + passwordHash.toLowerCase() + "%"));
      if (fullName != null && !fullName.isBlank())
        predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
      if (phone != null && !phone.isBlank())
        predicates.add(cb.like(cb.lower(root.get("phone")), "%" + phone.toLowerCase() + "%"));
      if (active != null) predicates.add(cb.equal(root.get("active"), active));
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
