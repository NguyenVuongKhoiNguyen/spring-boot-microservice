package com.poly.notification.dtos.responses;

import com.poly.notification.models.BookingNotification;
import java.time.Instant;

public record BookingNotificationResponse(
    Long id,
    Long userId,
    String title,
    String message,
    String type,
    Long referenceId,
    Boolean isRead,
    Instant readAt) {
  public static BookingNotificationResponse from(BookingNotification notification) {
    if (notification == null) {
      return null;
    }
    return new BookingNotificationResponse(
        notification.getId(),
        notification.getUserId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getType(),
        notification.getReferenceId(),
        notification.getIsRead(),
        notification.getReadAt());
  }
}
