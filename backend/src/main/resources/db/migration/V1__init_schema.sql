-- Flyway migration: V1__init_schema.sql
-- This file creates core schema for The Paddlers app (simplified version)

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  phone TEXT,
  rating NUMERIC(3,2),
  payment_profile_id TEXT,
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE organizations (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  timezone TEXT NOT NULL DEFAULT 'UTC',
  slug TEXT UNIQUE,
  branding JSONB DEFAULT '{}'::jsonb,
  address JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE locations (
  id BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  address JSONB DEFAULT '{}'::jsonb,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE courts (
  id BIGSERIAL PRIMARY KEY,
  location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  court_type TEXT,
  surface TEXT,
  indoor BOOLEAN DEFAULT FALSE,
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_courts_location ON courts(location_id);
CREATE INDEX idx_courts_org ON courts(org_id);

CREATE TABLE reservations (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
  court_id BIGINT NOT NULL REFERENCES courts(id) ON DELETE CASCADE,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
  status TEXT NOT NULL DEFAULT 'CONFIRMED',
  recurring_id BIGINT,
  price_amount NUMERIC(10,2),
  price_currency TEXT DEFAULT 'USD',
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (end_at > start_at)
);

ALTER TABLE reservations ADD COLUMN time_range tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED;
CREATE INDEX idx_reservations_court_timerange_gist ON reservations USING GIST (court_id, time_range);
CREATE INDEX idx_reservations_start_at ON reservations(start_at);
CREATE INDEX idx_reservations_end_at ON reservations(end_at);
CREATE INDEX idx_reservations_user ON reservations(created_by);
CREATE INDEX idx_reservations_court ON reservations(court_id);

CREATE TABLE booking_rules (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  applies_to_court BIGINT REFERENCES courts(id) ON DELETE SET NULL,
  name TEXT,
  rule_type TEXT NOT NULL,
  params JSONB DEFAULT '{}'::jsonb,
  effective_from TIMESTAMPTZ,
  effective_to TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_booking_rules_org ON booking_rules(org_id);

CREATE TABLE memberships (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  tier TEXT NOT NULL,
  starts_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ,
  recurring_payment BOOLEAN DEFAULT FALSE,
  credits INT DEFAULT 0,
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_memberships_user ON memberships(user_id);
CREATE INDEX idx_memberships_org ON memberships(org_id);

CREATE TABLE invoices (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  total_amount NUMERIC(12,2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'USD',
  status TEXT NOT NULL DEFAULT 'DRAFT',
  issued_at TIMESTAMPTZ DEFAULT now(),
  paid_at TIMESTAMPTZ,
  metadata JSONB DEFAULT '{}'::jsonb
);
CREATE INDEX idx_invoices_user ON invoices(user_id);
CREATE INDEX idx_invoices_status ON invoices(status);

CREATE TABLE invoice_items (
  id BIGSERIAL PRIMARY KEY,
  invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
  description TEXT,
  amount NUMERIC(12,2) NOT NULL,
  quantity INT DEFAULT 1
);

CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY,
  invoice_id BIGINT REFERENCES invoices(id) ON DELETE SET NULL,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  amount NUMERIC(12,2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'USD',
  gateway TEXT,
  gateway_payment_id TEXT,
  status TEXT NOT NULL DEFAULT 'PENDING',
  received_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_user ON payments(user_id);

CREATE TABLE refunds (
  id BIGSERIAL PRIMARY KEY,
  payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
  amount NUMERIC(12,2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'USD',
  gateway_refund_id TEXT,
  status TEXT NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE events (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
  title TEXT NOT NULL,
  description TEXT,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  capacity INT DEFAULT 0,
  price_amount NUMERIC(10,2),
  price_currency TEXT DEFAULT 'USD',
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ DEFAULT now(),
  CHECK (ends_at > starts_at)
);
ALTER TABLE events ADD COLUMN time_range tstzrange GENERATED ALWAYS AS (tstzrange(starts_at, ends_at, '[)')) STORED;
CREATE INDEX idx_events_timerange_gist ON events USING GIST (time_range);
CREATE INDEX idx_events_org ON events(org_id);

CREATE TABLE event_registrations (
  id BIGSERIAL PRIMARY KEY,
  event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT now(),
  status TEXT DEFAULT 'REGISTERED'
);
CREATE INDEX idx_event_reg_by_event ON event_registrations(event_id);

CREATE TABLE lessons (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  pro_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE SET NULL,
  student_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  price_amount NUMERIC(10,2),
  status TEXT DEFAULT 'SCHEDULED',
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ DEFAULT now(),
  CHECK (ends_at > starts_at)
);
ALTER TABLE lessons ADD COLUMN time_range tstzrange GENERATED ALWAYS AS (tstzrange(starts_at, ends_at, '[)')) STORED;
CREATE INDEX idx_lessons_pro ON lessons(pro_user_id);
CREATE INDEX idx_lessons_timerange_gist ON lessons USING GIST (time_range);

CREATE TABLE instructor_schedules (
  id BIGSERIAL PRIMARY KEY,
  pro_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  available_from TIME WITHOUT TIME ZONE,
  available_to TIME WITHOUT TIME ZONE,
  days_of_week SMALLINT[] DEFAULT ARRAY[]::smallint[],
  metadata JSONB DEFAULT '{}'::jsonb
);

CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  sku TEXT NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  price_amount NUMERIC(10,2) NOT NULL,
  currency TEXT DEFAULT 'USD',
  metadata JSONB DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE (org_id, sku)
);

CREATE TABLE inventory (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
  quantity INT DEFAULT 0,
  updated_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_inventory_product ON inventory(product_id);

CREATE TABLE pos_transactions (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  currency TEXT DEFAULT 'USD',
  created_at TIMESTAMPTZ DEFAULT now(),
  metadata JSONB DEFAULT '{}'::jsonb
);
CREATE TABLE pos_line_items (
  id BIGSERIAL PRIMARY KEY,
  pos_transaction_id BIGINT NOT NULL REFERENCES pos_transactions(id) ON DELETE CASCADE,
  product_id BIGINT REFERENCES products(id),
  description TEXT,
  unit_price NUMERIC(10,2),
  quantity INT DEFAULT 1
);

CREATE TABLE waitlist_entries (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
  court_id BIGINT REFERENCES courts(id) ON DELETE SET NULL,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  requested_start TIMESTAMPTZ,
  requested_end TIMESTAMPTZ,
  requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  metadata JSONB DEFAULT '{}'::jsonb
);
ALTER TABLE waitlist_entries ADD COLUMN time_range tstzrange GENERATED ALWAYS AS (tstzrange(requested_start, requested_end, '[)')) STORED;
CREATE INDEX idx_waitlist_court ON waitlist_entries(court_id);
CREATE INDEX idx_waitlist_timerange_gist ON waitlist_entries USING GIST (court_id, time_range);

CREATE TABLE checkins (
  id BIGSERIAL PRIMARY KEY,
  org_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  reservation_id BIGINT REFERENCES reservations(id) ON DELETE SET NULL,
  user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
  qr_code TEXT,
  checked_in_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  metadata JSONB DEFAULT '{}'::jsonb
);
CREATE INDEX idx_checkins_reservation ON checkins(reservation_id);
CREATE INDEX idx_checkins_user ON checkins(user_id);

-- sample overlap helper query comment
-- SELECT * FROM reservations WHERE court_id = :court_id AND time_range && tstzrange(:start_at::timestamptz, :end_at::timestamptz, '[)');

