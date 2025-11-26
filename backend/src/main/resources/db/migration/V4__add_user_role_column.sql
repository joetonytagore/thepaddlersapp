-- Flyway migration: V4__add_user_role_column.sql
-- Add a 'role' column to users for compatibility with the JPA User.role enum
ALTER TABLE users ADD COLUMN IF NOT EXISTS role varchar(255) NOT NULL DEFAULT 'ROLE_PLAYER';

-- Optional: keep user_roles normalized design, but populate role from user_roles if needed
-- (this is a simple backward-compatible column used by the application seed and queries)

-- Ensure the column exists for older databases where Hibernate created it implicitly.

