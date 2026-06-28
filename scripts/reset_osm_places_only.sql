-- Resets only OSM-imported places (and their derived junction rows),
-- while keeping your existing `city_seed` and any other non-OSM places.
--
-- Run before you import OSM POIs again (especially when importing city-by-city).

BEGIN;

-- Remove tag links for OSM places
DELETE FROM place_tags
USING places p
WHERE place_tags.place_id = p.id
  AND p.source = 'osm';

-- Remove image rows for OSM places (only removes image binaries/rows, not Unsplash ones for other sources)
DELETE FROM place_images
USING places p
WHERE place_images.place_id = p.id
  AND p.source = 'osm';

-- Remove the places themselves
DELETE FROM places
WHERE source = 'osm';

COMMIT;

