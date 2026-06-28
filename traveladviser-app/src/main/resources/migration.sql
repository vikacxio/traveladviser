-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

-- Places table with PostGIS geography
CREATE TABLE IF NOT EXISTS places (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location geography(Point, 4326) NOT NULL,
    avg_rating DOUBLE PRECISION DEFAULT 0,
    total_ratings INT DEFAULT 0,
    price_level INT CHECK (price_level BETWEEN 1 AND 5),
    best_season TEXT CHECK (best_season IN ('SUMMER','WINTER','MONSOON','ALL')),
    city TEXT,
    state TEXT,
    country TEXT,
    source TEXT,
    source_id TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_source UNIQUE (source, source_id)
);

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

-- Place-Tags junction table
CREATE TABLE IF NOT EXISTS place_tags (
    place_id BIGINT REFERENCES places(id) ON DELETE CASCADE,
    tag_id INT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (place_id, tag_id)
);

-- Cities table (optional but powerful)
CREATE TABLE IF NOT EXISTS cities (
    id SERIAL PRIMARY KEY,
    name TEXT,
    state TEXT,
    country TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_places_location ON places USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_place_tags_tag ON place_tags(tag_id);
CREATE INDEX IF NOT EXISTS idx_places_category ON places(category_id);
CREATE INDEX IF NOT EXISTS idx_places_city ON places(city);
CREATE INDEX IF NOT EXISTS idx_places_state ON places(state);
CREATE INDEX IF NOT EXISTS idx_places_avg_rating ON places(avg_rating DESC);

-- Insert sample categories
INSERT INTO categories (name) VALUES 
    ('Mountain'),
    ('Beach'),
    ('Temple'),
    ('Museum'),
    ('Park'),
    ('Restaurant'),
    ('Adventure'),
    ('Historical')
ON CONFLICT DO NOTHING;

-- Insert sample tags
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
    ('family_friendly')
ON CONFLICT DO NOTHING;

-- Sample places for testing (India locations)
INSERT INTO places (name, description, category_id, latitude, longitude, location, city, state, country, best_season, price_level, avg_rating, total_ratings)
VALUES 
    ('Manali', 'A beautiful hill station in Himalayas', 1, 32.2396, 77.1887, ST_MakePoint(77.1887, 32.2396)::geography, 'Manali', 'Himachal Pradesh', 'India', 'SUMMER', 2, 4.5, 250),
    ('Goa Beach', 'Famous beaches of Goa', 2, 15.2993, 73.8243, ST_MakePoint(73.8243, 15.2993)::geography, 'Goa', 'Goa', 'India', 'WINTER', 2, 4.3, 180),
    ('Varanasi Temple', 'Ancient temple on Ganges', 3, 25.3201, 82.9979, ST_MakePoint(82.9979, 25.3201)::geography, 'Varanasi', 'Uttar Pradesh', 'India', 'ALL', 1, 4.6, 320),
    ('National Museum Delhi', 'National museum with vast collection', 4, 28.6139, 77.2090, ST_MakePoint(77.2090, 28.6139)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 2, 4.4, 210),
    ('Lodi Garden', 'Beautiful garden in Delhi', 5, 28.5921, 77.2197, ST_MakePoint(77.2197, 28.5921)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 1, 4.2, 150)
ON CONFLICT DO NOTHING;
