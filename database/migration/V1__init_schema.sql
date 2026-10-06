CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE user_images (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_image_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE hotels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(255),
    star_rating INTEGER,
    check_in_time TIME NOT NULL DEFAULT '14:00',
    check_out_time TIME NOT NULL DEFAULT '12:00',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_hotel_star_rating
        CHECK (
            star_rating IS NULL
            OR star_rating BETWEEN 1 AND 5
        )
);

CREATE TABLE hotel_images (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_hotel_image_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotels(id)
        ON DELETE CASCADE
);

CREATE TABLE room_types (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    capacity INTEGER NOT NULL,
    bed_type VARCHAR(100),
    price_per_night NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_room_type_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotels(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_room_type_capacity
        CHECK (capacity > 0),

    CONSTRAINT chk_room_type_price
        CHECK (price_per_night >= 0),

    CONSTRAINT uq_hotel_room_type_name
        UNIQUE (hotel_id, name)
);

CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_room_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotels(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_room_room_type
        FOREIGN KEY (room_type_id)
        REFERENCES room_types(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_room_status
        CHECK (
            status IN (
                'AVAILABLE',
                'OCCUPIED',
                'MAINTENANCE',
                'INACTIVE'
            )
        ),

    CONSTRAINT uq_hotel_room_number
        UNIQUE (hotel_id, room_number)
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,

    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,

    guest_count INTEGER NOT NULL DEFAULT 1,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    total_price NUMERIC(12, 2) NOT NULL,

    special_request TEXT,

    del_if BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_booking_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotels(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_booking_room
        FOREIGN KEY (room_id)
        REFERENCES rooms(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_booking_dates
        CHECK (check_out_date > check_in_date),

    CONSTRAINT chk_booking_guest_count
        CHECK (guest_count > 0),

    CONSTRAINT chk_booking_total_price
        CHECK (total_price >= 0),

    CONSTRAINT chk_booking_status
        CHECK (
            status IN (
                'PENDING',
                'CONFIRMED',
                'CHECKED_IN',
                'CHECKED_OUT',
                'CANCELLED',
                'COMPLETED'
            )
        )
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    paid_at TIMESTAMPTZ,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_payment_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                'PENDING',
                'PAID',
                'FAILED',
                'REFUNDED'
            )
        )
);

CREATE TABLE notifications (
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

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

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

CREATE TABLE device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_device_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_device_platform
        CHECK (
            platform IN (
                'ANDROID',
                'IOS'
            )
        )
);

CREATE TABLE booking_status_history (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    del_if BOOLEAN NOT NULL DEFAULT FALSE,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_status_history
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_images_user_id
    ON user_images(user_id);



CREATE INDEX idx_hotels_city
    ON hotels(city);

CREATE INDEX idx_hotel_image_hotel_id
    ON hotel_images(hotel_id);

CREATE INDEX idx_room_types_hotel_id
    ON room_types(hotel_id);

CREATE INDEX idx_rooms_hotel_id
    ON rooms(hotel_id);

CREATE INDEX idx_rooms_room_type_id
    ON rooms(room_type_id);

CREATE INDEX idx_rooms_status
    ON rooms(status);

CREATE INDEX idx_bookings_user_id
    ON bookings(user_id);

CREATE INDEX idx_bookings_hotel_id
    ON bookings(hotel_id);

CREATE INDEX idx_bookings_room_id
    ON bookings(room_id);

CREATE INDEX idx_bookings_status
    ON bookings(status);

CREATE INDEX idx_bookings_dates
    ON bookings(check_in_date, check_out_date);

CREATE INDEX idx_bookings_room_dates
    ON bookings(room_id, check_in_date, check_out_date);

CREATE INDEX idx_payments_booking_id
    ON payments(booking_id);

CREATE INDEX idx_payments_status
    ON payments(status);

CREATE UNIQUE INDEX idx_payments_transaction_id
    ON payments(transaction_id)
    WHERE transaction_id IS NOT NULL;

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read);

CREATE INDEX idx_device_tokens_user_id
    ON device_tokens(user_id);

CREATE INDEX idx_booking_status_history_booking_id
    ON booking_status_history(booking_id);