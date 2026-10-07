package com.poly.hotel.repositories;

import com.poly.hotel.models.Booking;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BookingSpecification {
  private BookingSpecification() {}

  public static Specification<Booking> filter(
      Long id,
      Long userId,
      Long hotelId,
      Long roomId,
      LocalDate checkInDateFrom,
      LocalDate checkInDateTo,
      LocalDate checkOutDateFrom,
      LocalDate checkOutDateTo,
      Integer guestCountFrom,
      Integer guestCountTo,
      String status,
      BigDecimal totalPriceFrom,
      BigDecimal totalPriceTo,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("delIf"), false));
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
      if (hotelId != null) predicates.add(cb.equal(root.get("hotelId"), hotelId));
      if (roomId != null) predicates.add(cb.equal(root.get("roomId"), roomId));
      if (checkInDateFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), checkInDateFrom));
      if (checkInDateTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("checkInDate"), checkInDateTo));
      if (checkOutDateFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("checkOutDate"), checkOutDateFrom));
      if (checkOutDateTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), checkOutDateTo));
      if (guestCountFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("guestCount"), guestCountFrom));
      if (guestCountTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("guestCount"), guestCountTo));
      if (status != null && !status.isBlank())
        predicates.add(cb.like(cb.lower(root.get("status")), "%" + status.toLowerCase() + "%"));
      if (totalPriceFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("totalPrice"), totalPriceFrom));
      if (totalPriceTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("totalPrice"), totalPriceTo));
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
