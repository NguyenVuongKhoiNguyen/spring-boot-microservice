package com.poly.user.repositories;

import com.poly.user.models.DeviceToken;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class DeviceTokenSpecification {
  private DeviceTokenSpecification() {}

  public static Specification<DeviceToken> filter(
      Long id,
      Long userId,
      String token,
      String platform,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (userId != null) predicates.add(cb.equal(root.get("user").get("id"), userId));
      if (token != null && !token.isBlank())
        predicates.add(cb.like(cb.lower(root.get("token")), "%" + token.toLowerCase() + "%"));
      if (platform != null && !platform.isBlank())
        predicates.add(cb.like(cb.lower(root.get("platform")), "%" + platform.toLowerCase() + "%"));
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
