package com.poly.hotel.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "room_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Hotel hotel;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "capacity", nullable = false)
  private Integer capacity;

  @Column(name = "bed_type", length = 100)
  private String bedType;

  @Column(name = "price_per_night", nullable = false)
  private BigDecimal pricePerNight;

  @Column(name = "active", nullable = false)
  private Boolean active;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
