-- Indexes for reservations performance
CREATE INDEX idx_reservations_court_start_end ON reservations(court_id, start_time, end_time);
CREATE INDEX idx_reservations_active ON reservations(court_id, start_time, end_time) WHERE status = 'ACTIVE';

-- Index for matches
CREATE INDEX idx_matches_league_scheduled ON matches(league_id, scheduled_start);

-- Index for invoices
CREATE INDEX idx_invoices_org_created ON invoices(org_id, created_at);

-- Index for users
CREATE INDEX idx_users_org_email ON users(org_id, email);

-- Optionally run ANALYZE for query planner stats
ANALYZE reservations;
ANALYZE matches;
ANALYZE invoices;
ANALYZE users;
-- For full optimization, run VACUUM ANALYZE in ops scripts after large migrations.

