package com.poly.user.models;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "image_url", nullable = false, length = 1000)
  private String imageUrl;

  @Column(name = "del_if", nullable = false)
  private Boolean delIf;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
