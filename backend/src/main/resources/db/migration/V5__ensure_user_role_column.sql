-- Flyway migration: V5__ensure_user_role_column.sql
-- Ensure the users.role column exists (idempotent)
-- This is a safe, non-destructive migration: it will only add the column if missing.
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS role varchar(255) NOT NULL DEFAULT 'ROLE_PLAYER';

-- Keep this migration intentionally simple and idempotent so it can be applied
-- to databases where V4 may have been skipped or not packaged.

