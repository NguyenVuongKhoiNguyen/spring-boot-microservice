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
@Table(name = "booking_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Booking booking;

  @Column(name = "old_status", length = 50)
  private String oldStatus;

  @Column(name = "new_status", nullable = false, length = 50)
  private String newStatus;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;
}
