package com.poly.user.repositories;

import com.poly.user.models.UserImage;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class UserImageSpecification {
  private UserImageSpecification() {}

  public static Specification<UserImage> filter(
      Long id,
      Long userId,
      String imageUrl,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (userId != null) predicates.add(cb.equal(root.get("user").get("id"), userId));
      if (imageUrl != null && !imageUrl.isBlank())
        predicates.add(cb.like(cb.lower(root.get("imageUrl")), "%" + imageUrl.toLowerCase() + "%"));
      if (delIf != null) predicates.add(cb.equal(root.get("delIf"), delIf));
      if (createdAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
      if (createdAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
