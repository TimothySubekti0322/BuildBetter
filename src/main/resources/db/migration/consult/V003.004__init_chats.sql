CREATE TABLE IF NOT EXISTS chats (
  id         UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
  room_id    UUID           NOT NULL REFERENCES rooms(id),
  sender     UUID           NOT NULL,
  content    TEXT           NOT NULL,
  type       VARCHAR(25)    NOT NULL,
  created_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);