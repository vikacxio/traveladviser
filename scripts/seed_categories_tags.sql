-- Seed your fixed categories + tags lists.
-- Run this once (or re-run safely) before importing places.

INSERT INTO categories (name) VALUES
  ('Mountain'),
  ('Beach'),
  ('Temple'),
  ('Museum'),
  ('Park'),
  ('Fort'),
  ('Lake'),
  ('Waterfall'),
  ('Garden'),
  ('Historical'),
  ('Adventure'),
  ('Wildlife'),
  ('Shopping'),
  ('Restaurant')
ON CONFLICT (name) DO NOTHING;

INSERT INTO tags (name) VALUES
  ('cool_place'),
  ('water_spot'),
  ('snow'),
  ('fireplace'),
  ('indoor'),
  ('museum'),
  ('cafe'),
  ('outdoor'),
  ('beach'),
  ('hiking'),
  ('budget_friendly'),
  ('luxury'),
  ('family_friendly'),
  ('adventure'),
  ('romantic'),
  ('photography'),
  ('spiritual'),
  ('historical'),
  ('nature_lover'),
  ('wildlife')
ON CONFLICT (name) DO NOTHING;

