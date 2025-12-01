-- V1__create_league_tables.sql
-- All tables include organization_id (UUID) for multi-tenancy

CREATE TABLE leagues (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    format VARCHAR(50) NOT NULL, -- e.g., round-robin, ladder
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE league_groups (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE league_players (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE matches (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    group_id UUID REFERENCES league_groups(id),
    player1_id UUID NOT NULL,
    player2_id UUID NOT NULL,
    scheduled_time TIMESTAMP WITH TIME ZONE,
    completed_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    winner_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE tournaments (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE
);

CREATE TABLE tournament_entries (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE checkins (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    checkin_time TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE scores (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    score INT NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE ladder_positions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    player_id UUID NOT NULL,
    position INT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE tournament_brackets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round INT NOT NULL,
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE
);
