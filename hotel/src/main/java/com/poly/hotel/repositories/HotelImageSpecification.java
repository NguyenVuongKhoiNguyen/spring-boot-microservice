package com.poly.hotel.repositories;

import com.poly.hotel.models.HotelImage;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class HotelImageSpecification {
  private HotelImageSpecification() {}

  public static Specification<HotelImage> filter(
      Long id,
      Long hotelId,
      String imageUrl,
      Boolean isPrimary,
      Integer sortOrder,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (hotelId != null) predicates.add(cb.equal(root.get("hotel").get("id"), hotelId));
      if (imageUrl != null && !imageUrl.isBlank())
        predicates.add(cb.like(cb.lower(root.get("imageUrl")), "%" + imageUrl.toLowerCase() + "%"));
      if (isPrimary != null) predicates.add(cb.equal(root.get("isPrimary"), isPrimary));
      if (sortOrder != null) predicates.add(cb.equal(root.get("sortOrder"), sortOrder));
      if (delIf != null) predicates.add(cb.equal(root.get("delIf"), delIf));
      if (createdAtFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
      if (createdAtTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
