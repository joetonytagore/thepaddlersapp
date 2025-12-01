CREATE TYPE queueable_type AS ENUM ('LEAGUE_EVENT', 'TOURNAMENT', 'MATCH');

CREATE TABLE waitlist_entries (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    queueable_type queueable_type NOT NULL,
    queueable_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    offer_expires_at TIMESTAMP,
    offer_accepted BOOLEAN DEFAULT FALSE
);

