CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(128) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);

