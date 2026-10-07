package com.poly.notification.repositories;

import com.poly.notification.models.BookingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingNotificationRepository
    extends JpaRepository<BookingNotification, Long>,
        JpaSpecificationExecutor<BookingNotification> {}
