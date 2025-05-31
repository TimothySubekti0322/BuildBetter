-- ============================================================
-- V003.011__alter_consultations_foreign_key_constraint.sql
-- ------------------------------------------------------------
-- Purpose  : Adapt FK actions on consultations *
--            user_id      -> users(id)       : ON DELETE SET NULL
--            architect_id -> architects(id)  : ON DELETE SET NULL
--            room_id      -> rooms(id)       : unchanged (RESTRICT)
-- ============================================================

BEGIN;

----------------------------------------------------------------
-- 1. Drop existing FK constraints
----------------------------------------------------------------
ALTER TABLE consultations
    DROP CONSTRAINT IF EXISTS consults_user_id_fkey,
    DROP CONSTRAINT IF EXISTS consults_architect_id_fkey;

----------------------------------------------------------------
-- 2. Allow NULLs for columns that will receive SET NULL
----------------------------------------------------------------
ALTER TABLE consultations
    ALTER COLUMN user_id      DROP NOT NULL,
    ALTER COLUMN architect_id DROP NOT NULL;

----------------------------------------------------------------
-- 3. Re-add the constraints with the new referential actions
----------------------------------------------------------------
ALTER TABLE consultations
    ADD CONSTRAINT consults_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT consults_architect_id_fkey
        FOREIGN KEY (architect_id)
        REFERENCES architects(id)
        ON DELETE SET NULL;

COMMIT;