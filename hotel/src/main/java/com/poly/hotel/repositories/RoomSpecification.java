package com.poly.hotel.repositories;

import com.poly.hotel.models.Room;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RoomSpecification {
  private RoomSpecification() {}

  public static Specification<Room> filter(
      Long id,
      Long hotelId,
      Long roomTypeId,
      String roomNumber,
      String status,
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
      if (roomTypeId != null) predicates.add(cb.equal(root.get("roomType").get("id"), roomTypeId));
      if (roomNumber != null && !roomNumber.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("roomNumber")), "%" + roomNumber.toLowerCase() + "%"));
      if (status != null && !status.isBlank())
        predicates.add(cb.like(cb.lower(root.get("status")), "%" + status.toLowerCase() + "%"));
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
