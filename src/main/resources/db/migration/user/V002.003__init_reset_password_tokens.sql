CREATE TABLE IF NOT EXISTS reset_password_tokens (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID            NOT NULL,
    hashed_token    TEXT            NOT NULL,
    expired_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    is_used         BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_reset_password_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_rpt_unused_not_expired
    ON reset_password_tokens (is_used, expired_at);