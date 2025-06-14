CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS architects (
    id            UUID          PRIMARY KEY     DEFAULT uuid_generate_v4(),
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password      Varchar(255)  NOT NULL,
    username      VARCHAR(255)  NOT NULL,
    photo         TEXT,
    province      VARCHAR(255)  NOT NULL,
    city          VARCHAR(255)  NOT NULL,
    phone_number  VARCHAR(30),
    experience    REAL,
    rate_online   INT           NOT NULL,
    rate_offline  INT           NOT NULL,
    portfolio     TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER architects_set_updated_at
  BEFORE UPDATE
  ON architects
  FOR EACH ROW
EXECUTE FUNCTION trigger_set_updated_at();