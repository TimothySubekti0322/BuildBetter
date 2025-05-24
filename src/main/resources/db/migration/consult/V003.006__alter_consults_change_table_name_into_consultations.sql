ALTER TABLE consults RENAME TO consultations;

ALTER TABLE consultations RENAME CONSTRAINT consults_pkey TO consultations_pkey;

ALTER TABLE payments
  DROP CONSTRAINT IF EXISTS payments_consult_id_fkey;

ALTER TABLE payments RENAME COLUMN consult_id TO consultation_id;

ALTER TABLE payments
  ADD CONSTRAINT payments_consult_id_fkey
  FOREIGN KEY (consultation_id)
  REFERENCES consultations(id)
  ON DELETE CASCADE;
