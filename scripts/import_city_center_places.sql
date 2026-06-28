-- Creates one "city center" Place for every row in `cities`.
-- This is a baseline so your UI can show places for all cities immediately.
--
-- It does NOT add real tourist attractions (those require a POI dataset like OSM).

INSERT INTO places (
  name,
  description,
  category_id,
  latitude,
  longitude,
  location,
  avg_rating,
  total_ratings,
  price_level,
  best_season,
  city,
  state,
  country,
  source,
  source_id
)
SELECT
  c.name || ' - Travel Guide' AS name,
  'Explore ' || c.name || ' and discover curated trips and nearby attractions.' AS description,
  (SELECT id FROM categories WHERE name = 'Historical' LIMIT 1) AS category_id,
  c.latitude,
  c.longitude,
  ST_MakePoint(c.longitude, c.latitude)::geography AS location,
  0.0 AS avg_rating,
  0 AS total_ratings,
  2 AS price_level,
  'ALL' AS best_season,
  c.name AS city,
  c.state,
  c.country,
  'city_seed' AS source,
  ('city_' || c.id::text) AS source_id
FROM cities c
ON CONFLICT (source, source_id) DO NOTHING;

