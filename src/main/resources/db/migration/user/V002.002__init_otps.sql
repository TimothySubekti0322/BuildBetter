CREATE TABLE IF NOT EXISTS otps (
    id          UUID        PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id     UUID        NOT NULL,
    hashed_otp  TEXT        NOT NULL,
    expired_at  TIMESTAMP   NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    is_used     BOOLEAN     NOT NULL DEFAULT FALSE,
    attempt     INTEGER     NOT NULL DEFAULT 0,

    CONSTRAINT fk_otps_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_otps_user_active
    ON otps (user_id, is_used, expired_at);