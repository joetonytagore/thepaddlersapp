-- V20251130__add_membership_credits.sql
-- Add membership plan credit column and membership credits remaining

ALTER TABLE IF EXISTS membership_plan
  ADD COLUMN IF NOT EXISTS credits_per_period integer DEFAULT 0;

COMMENT ON COLUMN membership_plan.credits_per_period IS 'Booking credits granted per billing period (integer)';

ALTER TABLE IF EXISTS membership
  ADD COLUMN IF NOT EXISTS credits_remaining integer DEFAULT 0;

COMMENT ON COLUMN membership.credits_remaining IS 'Available booking credits for the current membership period (integer)';

-- Backfill existing memberships with the plan default when credits_remaining is NULL
UPDATE membership
SET credits_remaining = COALESCE(mp.credits_per_period, 0)
FROM membership_plan mp
WHERE membership.plan_id = mp.id
  AND (membership.credits_remaining IS NULL OR membership.credits_remaining = 0);

-- Ensure NOT NULL with default for safety going forward
ALTER TABLE membership_plan ALTER COLUMN credits_per_period SET NOT NULL;
ALTER TABLE membership ALTER COLUMN credits_remaining SET NOT NULL;

