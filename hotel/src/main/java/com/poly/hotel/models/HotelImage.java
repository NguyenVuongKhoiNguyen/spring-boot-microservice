package com.poly.hotel.models;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "hotel_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hotel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Hotel hotel;

  @Column(name = "image_url", nullable = false, length = 1000)
  private String imageUrl;

  @Column(name = "is_primary", nullable = false)
  private Boolean isPrimary;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
