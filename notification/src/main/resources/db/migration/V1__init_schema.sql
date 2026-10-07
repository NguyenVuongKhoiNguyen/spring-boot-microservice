-- user_id points to the user service (another database): plain column, no foreign key.
CREATE TABLE booking_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ,

    CONSTRAINT chk_notification_type
        CHECK (
            type IN (
                'BOOKING_CONFIRMED',
                'BOOKING_CANCELLED',
                'CHECK_IN_REMINDER',
                'CHECK_OUT_REMINDER',
                'PAYMENT_SUCCESS',
                'PAYMENT_FAILED',
                'GENERAL'
            )
        )
);

CREATE INDEX idx_notifications_user_id
    ON booking_notifications(user_id);

CREATE INDEX idx_notifications_user_unread
    ON booking_notifications(user_id, is_read);