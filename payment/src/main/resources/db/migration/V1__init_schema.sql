-- booking_id points to the hotel service (another database): plain column, no foreign key.
CREATE TABLE booking_payments (
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

CREATE INDEX idx_payments_booking_id
    ON booking_payments(booking_id);

CREATE INDEX idx_payments_status
    ON booking_payments(status);

CREATE UNIQUE INDEX idx_payments_transaction_id
    ON booking_payments(transaction_id)
    WHERE transaction_id IS NOT NULL;