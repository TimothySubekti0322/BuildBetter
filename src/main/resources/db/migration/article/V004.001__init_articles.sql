CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TABLE IF NOT EXISTS articles (
    id          UUID           PRIMARY KEY   DEFAULT uuid_generate_v4(),
    author      VARCHAR(255),
    title       TEXT           NOT NULL,
    banner      TEXT,                              
    content     TEXT,                               
    created_at  TIMESTAMPTZ    NOT NULL      DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ    NOT NULL      DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER articles_set_updated_at
  BEFORE UPDATE
  ON articles
  FOR EACH ROW
EXECUTE FUNCTION trigger_set_updated_at();
