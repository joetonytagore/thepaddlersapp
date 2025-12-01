CREATE TABLE devices (
    device_id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(32) NOT NULL,
    push_token VARCHAR(255) NOT NULL,
    app_version VARCHAR(32) NOT NULL
);
CREATE INDEX idx_devices_user_id ON devices(user_id);

