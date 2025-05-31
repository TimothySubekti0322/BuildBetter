-- ============================================================
-- V003.012__alter_rooms_foreign_key_constraint.sql
-- ------------------------------------------------------------
-- 1) Make rooms.architect_id and rooms.user_id nullable
-- 2) Change their FK actions to ON DELETE SET NULL
-- 3) Replace the old UNIQUE constraint with a partial index
--    so uniqueness applies only when BOTH IDs are still present
-- ============================================================

BEGIN;

----------------------------------------------------------------
-- 1. Drop existing FK constraints we want to change
----------------------------------------------------------------
ALTER TABLE rooms
    DROP CONSTRAINT IF EXISTS rooms_architect_id_fkey,
    DROP CONSTRAINT IF EXISTS rooms_user_id_fkey;

----------------------------------------------------------------
-- 2. Allow NULLs on the FK columns
----------------------------------------------------------------
ALTER TABLE rooms
    ALTER COLUMN architect_id DROP NOT NULL,
    ALTER COLUMN user_id      DROP NOT NULL;

----------------------------------------------------------------
-- 3. Re-add the constraints with ON DELETE SET NULL
----------------------------------------------------------------
ALTER TABLE rooms
    ADD CONSTRAINT rooms_architect_id_fkey
        FOREIGN KEY (architect_id)
        REFERENCES architects(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT rooms_user_id_fkey
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL;

----------------------------------------------------------------
-- 4. Preserve the “one active room per pair” rule
--    by replacing the old UNIQUE constraint with a
--    partial unique index that ignores rows where either
--    FK has become NULL.
----------------------------------------------------------------
ALTER TABLE rooms
    DROP CONSTRAINT IF EXISTS uq_rooms_architect_user;

-- Create new partial unique index
CREATE UNIQUE INDEX uq_rooms_architect_user_not_null
    ON rooms (architect_id, user_id)
    WHERE architect_id IS NOT NULL
      AND user_id      IS NOT NULL;

COMMIT;
