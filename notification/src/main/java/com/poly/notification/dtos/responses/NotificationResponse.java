package com.poly.notification.dtos.responses;

import com.poly.notification.models.Notification;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    Long userId,
    String title,
    String message,
    String type,
    Long referenceId,
    Boolean isRead,
    Instant readAt) {
  public static NotificationResponse from(Notification notification) {
    if (notification == null) {
      return null;
    }
    return new NotificationResponse(
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
