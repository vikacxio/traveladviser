-- Existing databases may still have an old unique constraint that only covers (user_id, owes_to).
-- That causes cross-group duplicates and makes balance recalculation fail.
-- Remove the old constraint, rebuild balances from expenses, and enforce the correct group-aware constraint.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk2gng0mruglm6dvgk2xppb93mc'
    ) THEN
        ALTER TABLE balances DROP CONSTRAINT uk2gng0mruglm6dvgk2xppb93mc;
    END IF;
END $$;

-- Clear stale rows so balances are rebuilt deterministically from expenses.
DELETE FROM balances;

-- Enforce one balance row per debtor/creditor pair within a group.
CREATE UNIQUE INDEX IF NOT EXISTS uk_balances_group_pair
    ON balances (user_id, owes_to, group_id);
