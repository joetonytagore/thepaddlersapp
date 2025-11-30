ALTER TABLE users ADD COLUMN username VARCHAR(255);
ALTER TABLE users ADD COLUMN password_hash VARCHAR(255);
-- For multi-tenancy support
ALTER TABLE users ADD COLUMN organization_id BIGINT;
-- You may need to add organization_id to other tables as well for full multi-tenancy.

