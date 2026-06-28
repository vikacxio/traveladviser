-- Import India cities into the `cities` table (client-side \copy).
-- This expects `india_cities.csv` to exist in the same directory where you run psql.

-- Ensure table exists (matches your current schema)
CREATE TABLE IF NOT EXISTS cities (
  id SERIAL PRIMARY KEY,
  name TEXT,
  state TEXT,
  country TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION
);

TRUNCATE TABLE cities;

-- Load from CSV on the psql client machine
\copy cities (name, state, country, latitude, longitude) FROM 'india_cities.csv' WITH (FORMAT csv, HEADER true);

