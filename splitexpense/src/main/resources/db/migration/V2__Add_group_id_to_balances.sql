-- Delete existing balances with null group_id to clean up old data
-- Balances will be recalculated from expenses when they are created
DELETE FROM balances WHERE group_id IS NULL;

-- Add NOT NULL constraint to group_id
ALTER TABLE balances ALTER COLUMN group_id SET NOT NULL;
