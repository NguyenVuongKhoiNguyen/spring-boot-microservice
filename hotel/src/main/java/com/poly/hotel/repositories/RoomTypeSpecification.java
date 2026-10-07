package com.poly.hotel.repositories;

import com.poly.hotel.models.RoomType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RoomTypeSpecification {
  private RoomTypeSpecification() {}

  public static Specification<RoomType> filter(
      Long id,
      Long hotelId,
      String name,
      String description,
      Integer capacityFrom,
      Integer capacityTo,
      String bedType,
      BigDecimal pricePerNightFrom,
      BigDecimal pricePerNightTo,
      Boolean active,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("delIf"), false));
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (hotelId != null) predicates.add(cb.equal(root.get("hotel").get("id"), hotelId));
      if (name != null && !name.isBlank())
        predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
      if (description != null && !description.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
      if (capacityFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), capacityFrom));
      if (capacityTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), capacityTo));
      if (bedType != null && !bedType.isBlank())
        predicates.add(cb.like(cb.lower(root.get("bedType")), "%" + bedType.toLowerCase() + "%"));
      if (pricePerNightFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerNight"), pricePerNightFrom));
      if (pricePerNightTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("pricePerNight"), pricePerNightTo));
      if (active != null) predicates.add(cb.equal(root.get("active"), active));
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
