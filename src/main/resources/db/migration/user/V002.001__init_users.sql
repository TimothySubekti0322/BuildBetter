CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id            UUID          PRIMARY KEY     DEFAULT uuid_generate_v4(),
    city          VARCHAR(255),
    email         VARCHAR(255)  NOT NULL UNIQUE,
    is_verified   BOOLEAN       NOT NULL DEFAULT FALSE,
    password      TEXT          NOT NULL,
    phone_number  VARCHAR(30)   NOT NULL,
    photos        TEXT,
    province      VARCHAR(255),
    role          VARCHAR(30)   DEFAULT 'user',
    username      VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users (phone_number);
CREATE INDEX IF NOT EXISTS idx_users_email        ON users (email);