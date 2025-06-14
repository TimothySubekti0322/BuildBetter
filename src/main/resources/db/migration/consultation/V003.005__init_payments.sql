CREATE TABLE IF NOT EXISTS payments (
  id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
  consult_id      UUID         NOT NULL REFERENCES consults(id),
  proof_payment   TEXT         NOT NULL,  -- link to proof
  payment_method  VARCHAR(100) NOT NULL,
  sender          VARCHAR(255) NOT NULL
);