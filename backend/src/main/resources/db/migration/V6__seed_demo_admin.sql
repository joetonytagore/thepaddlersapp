-- Seed demo admin user for local/dev only
DO $$
BEGIN
  IF current_setting('is_demo_seed', true) = 'true' THEN
    INSERT INTO users (id, name, email, role, password_hash)
    VALUES (999, 'Demo Admin', 'admin@demo.local', 'ADMIN', '$2a$10$demoHash')
    ON CONFLICT (id) DO NOTHING;
  END IF;
END $$;

