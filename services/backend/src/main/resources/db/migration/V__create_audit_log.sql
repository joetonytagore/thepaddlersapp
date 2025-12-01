CREATE TABLE audit_log (
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_org_id ON audit_log(org_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_actor_id ON audit_log(actor_id);
);
    created_at TIMESTAMP NOT NULL
    details JSONB NOT NULL,
    org_id UUID NOT NULL,
    target_id UUID,
    action VARCHAR(64) NOT NULL,
    actor_id UUID NOT NULL,
    id UUID PRIMARY KEY,

