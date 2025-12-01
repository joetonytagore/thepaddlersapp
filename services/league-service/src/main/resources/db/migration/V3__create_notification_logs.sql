CREATE TABLE notification_logs (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    user_id UUID,
    type VARCHAR(32) NOT NULL,
    target_id UUID,
    sent_at TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,
    message TEXT
);

