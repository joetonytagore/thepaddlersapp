-- V2: membership plans, memberships, payments, stripe_events

CREATE TABLE IF NOT EXISTS membership_plans (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT,
  name TEXT NOT NULL,
  price_cents BIGINT NOT NULL,
  interval TEXT NOT NULL,
  stripe_price_id TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS memberships (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT,
  user_id BIGINT,
  plan_id BIGINT,
  stripe_subscription_id TEXT,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  current_period_start TIMESTAMPTZ,
  current_period_end TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payment_methods (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  stripe_payment_method_id TEXT,
  card_brand TEXT,
  last4 TEXT,
  exp_month INT,
  exp_year INT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payments (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT,
  user_id BIGINT,
  amount_cents BIGINT,
  currency TEXT,
  stripe_payment_intent_id TEXT,
  status TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stripe_events (
  id BIGSERIAL PRIMARY KEY,
  event_id TEXT UNIQUE,
  payload JSONB,
  received_at TIMESTAMPTZ DEFAULT now(),
  processed BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_memberships_user ON memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_user ON payments(user_id);

