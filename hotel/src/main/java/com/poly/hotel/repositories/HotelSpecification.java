package com.poly.hotel.repositories;

import com.poly.hotel.models.Hotel;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class HotelSpecification {
  private HotelSpecification() {}

  public static Specification<Hotel> filter(
      Long id,
      String name,
      String description,
      String address,
      String city,
      String country,
      String phone,
      String email,
      Integer starRatingFrom,
      Integer starRatingTo,
      LocalTime checkInTimeFrom,
      LocalTime checkInTimeTo,
      LocalTime checkOutTimeFrom,
      LocalTime checkOutTimeTo,
      Boolean active,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("delIf"), false));
      if (id != null) predicates.add(cb.equal(root.get("id"), id));
      if (name != null && !name.isBlank())
        predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
      if (description != null && !description.isBlank())
        predicates.add(
            cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
      if (address != null && !address.isBlank())
        predicates.add(cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
      if (city != null && !city.isBlank())
        predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
      if (country != null && !country.isBlank())
        predicates.add(cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%"));
      if (phone != null && !phone.isBlank())
        predicates.add(cb.like(cb.lower(root.get("phone")), "%" + phone.toLowerCase() + "%"));
      if (email != null && !email.isBlank())
        predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
      if (starRatingFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("starRating"), starRatingFrom));
      if (starRatingTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("starRating"), starRatingTo));
      if (checkInTimeFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), checkInTimeFrom));
      if (checkInTimeTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), checkInTimeTo));
      if (checkOutTimeFrom != null)
        predicates.add(cb.greaterThanOrEqualTo(root.get("checkOutTime"), checkOutTimeFrom));
      if (checkOutTimeTo != null)
        predicates.add(cb.lessThanOrEqualTo(root.get("checkOutTime"), checkOutTimeTo));
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
