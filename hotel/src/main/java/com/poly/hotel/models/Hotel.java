package com.poly.hotel.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "address", nullable = false, length = 500)
  private String address;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "email", length = 255)
  private String email;

  @Column(name = "star_rating")
  private Integer starRating;

  @Column(name = "check_in_time", nullable = false)
  private LocalTime checkInTime;

  @Column(name = "check_out_time", nullable = false)
  private LocalTime checkOutTime;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
