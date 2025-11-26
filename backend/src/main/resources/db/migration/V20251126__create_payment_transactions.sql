-- V20251126__create_payment_transactions.sql
-- Purpose: Create payment_transactions table to record Stripe payment intents/charges
-- Author: automated change by plan

CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_uuid UUID NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    user_id BIGINT,
    amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    stripe_payment_intent_id VARCHAR(128),
    stripe_charge_id VARCHAR(128),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Foreign keys (adjust target table/schema if different in your project)
ALTER TABLE payment_transactions
    ADD CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- If your users table is named differently, adjust below
ALTER TABLE payment_transactions
    ADD CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payment_transactions_booking_id ON payment_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_user_id ON payment_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX IF NOT EXISTS idx_payment_transactions_created_at ON payment_transactions(created_at);

-- Unique index on Stripe payment intent id to help idempotency
CREATE UNIQUE INDEX IF NOT EXISTS ux_payment_transactions_stripe_payment_intent_id ON payment_transactions(stripe_payment_intent_id);

-- Optional: trigger to update updated_at on row modification
CREATE OR REPLACE FUNCTION touch_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_payment_transactions_updated_at ON payment_transactions;
CREATE TRIGGER trg_payment_transactions_updated_at
    BEFORE UPDATE ON payment_transactions
    FOR EACH ROW EXECUTE PROCEDURE touch_updated_at_column();

-- Recommended status values: 'pending', 'succeeded', 'failed', 'refunded', 'cancelled'

COMMENT ON TABLE payment_transactions IS 'Records payment transactions and Stripe identifiers for bookings';

