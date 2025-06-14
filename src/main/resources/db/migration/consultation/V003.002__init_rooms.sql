CREATE TABLE IF NOT EXISTS rooms (
  id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  architect_id UUID         NOT NULL REFERENCES architects(id),
  user_id      UUID         NOT NULL REFERENCES users(id),
  start_time   TIMESTAMPTZ  NOT NULL,
  end_time     TIMESTAMPTZ  NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER rooms_set_updated_at
  BEFORE UPDATE ON rooms
  FOR EACH ROW
  EXECUTE FUNCTION trigger_set_updated_at();
