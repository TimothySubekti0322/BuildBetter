CREATE TABLE IF NOT EXISTS consults (
  id            UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id       UUID           NOT NULL REFERENCES users(id),
  architect_id  UUID           NOT NULL REFERENCES architects(id),
  room_id       UUID           REFERENCES rooms(id),
  type          VARCHAR(25)    NOT NULL,  -- 'offline' or 'online'
  total         INTEGER        NOT NULL,  -- price in smallest currency unit
  status        VARCHAR(50)    NOT NULL,  -- e.g. waiting, canceled, scheduled, in-progress, ended
  reason        TEXT,                   -- cancellation reason
  start_date    TIMESTAMPTZ    NOT NULL,
  end_date      TIMESTAMPTZ    NOT NULL,
  created_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);