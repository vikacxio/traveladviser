import argparse
import csv
import json
import io
import math
import os
import re
import sys
import urllib.parse
import urllib.request
from urllib.error import HTTPError, URLError
from typing import Any, Dict, Iterable, List, Optional, Tuple


DEFAULT_OVERPASS_URL = "https://overpass-api.de/api/interpreter"
OVERPASS_FALLBACK_URLS = [
    "https://overpass-api.de/api/interpreter",
    "https://lz4.overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
]


def sql_escape(value: str) -> str:
    return value.replace("'", "''")


def osm_center_coords(el: Dict[str, Any]) -> Optional[Tuple[float, float]]:
    """
    Overpass elements return:
      - node: el['lat'], el['lon']
      - way/relation: el['center']['lat'], el['center']['lon'] when using `out center tags`
    """
    if "lat" in el and "lon" in el:
        return float(el["lat"]), float(el["lon"])
    center = el.get("center")
    if center and "lat" in center and "lon" in center:
        return float(center["lat"]), float(center["lon"])
    return None


def map_osm_tags_to_category_and_tags(tags: Dict[str, Any]) -> Tuple[Optional[str], List[str]]:
    """
    Heuristic mapping from OSM tags to your app categories/tags.

    Important: This is mapping logic. It is not "truth" from OSM.
    Names + coordinates come from OSM; categories/tags are derived deterministically.
    """
    tourism = (tags.get("tourism") or "").lower()
    amenity = (tags.get("amenity") or "").lower()
    historic = (tags.get("historic") or "").lower()
    waterway = (tags.get("waterway") or "").lower()
    natural = (tags.get("natural") or "").lower()
    leisure = (tags.get("leisure") or "").lower()
    water = (tags.get("water") or "").lower()

    category = None
    out_tags: List[str] = []

    def add(t: str) -> None:
        if t not in out_tags:
            out_tags.append(t)

    # Beaches
    if tourism == "beach" or natural == "coastline":
        category = "Beach"
        add("beach")
        add("water_spot")
        add("outdoor")
        add("photography")

    # Museums / indoor cultural
    elif tourism in ("museum", "gallery", "information", "information_center"):
        category = "Museum"
        add("museum")
        add("indoor")
        add("historical")
        add("photography")

    # Parks / gardens
    elif tourism in ("park", "picnic_site"):
        category = "Park"
        add("outdoor")
        add("photography")

    elif tourism == "garden" or leisure in ("garden", "botanical_garden"):
        category = "Garden"
        add("outdoor")
        add("photography")

    # Wildlife
    elif tourism in ("zoo", "aquarium"):
        category = "Wildlife"
        add("wildlife")
        add("nature_lover")
        add("outdoor")
        add("photography")

    # Temples / place of worship
    elif amenity == "place_of_worship":
        category = "Temple"
        add("spiritual")
        add("historical")
        add("photography")

    # Forts / monuments
    elif historic in ("castle", "fortress", "fort"):
        category = "Fort"
        add("historical")
        add("photography")

    elif historic in ("monument", "memorial"):
        category = "Historical"
        add("historical")
        add("spiritual")
        add("photography")

    # Waterfall
    elif waterway == "waterfall":
        category = "Waterfall"
        add("water_spot")
        add("outdoor")
        add("nature_lover")
        add("photography")

    # Lakes (only when OSM hints it's a lake/lagoon/reservoir)
    elif natural == "water" and water in ("lake", "lagoon", "reservoir"):
        category = "Lake"
        add("water_spot")
        add("outdoor")
        add("nature_lover")
        add("photography")

    # Adventure-ish viewpoints/attractions
    elif tourism in ("viewpoint", "attraction", "theme_park"):
        category = "Adventure"
        add("adventure")
        add("outdoor")
        add("photography")

    # Mountain peaks
    elif natural in ("peak", "volcano"):
        category = "Mountain"
        add("adventure")
        add("nature_lover")
        add("outdoor")
        add("photography")

    # If we didn't match anything, keep null category and no tags.
    return category, out_tags


def overpass_query(radius_m: int, lat: float, lon: float) -> str:
    # Only POIs that are plausibly "touristic" from OSM tagging.
    # We fetch nodes/ways/relations, and request `out center tags` so we can always read coordinates.
    return f"""
[out:json][timeout:90];
(
  node["tourism"~"museum|gallery|information|information_center|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});
  way["tourism"~"museum|gallery|information|information_center|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});
  relation["tourism"~"museum|gallery|information|information_center|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});

  node["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});
  way["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});
  relation["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});

  node["historic"~"castle|fortress|fort|monument|memorial"](around:{radius_m},{lat},{lon});
  way["historic"~"castle|fortress|fort|monument|memorial"](around:{radius_m},{lat},{lon});
  relation["historic"~"castle|fortress|fort|monument|memorial"](around:{radius_m},{lat},{lon});

  node["waterway"="waterfall"](around:{radius_m},{lat},{lon});
  way["waterway"="waterfall"](around:{radius_m},{lat},{lon});

  node["natural"="water"]["water"~"lake|lagoon|reservoir"](around:{radius_m},{lat},{lon});
  way["natural"="water"]["water"~"lake|lagoon|reservoir"](around:{radius_m},{lat},{lon});

  node["natural"~"peak|volcano"](around:{radius_m},{lat},{lon});
  way["natural"~"peak|volcano"](around:{radius_m},{lat},{lon});
);
out center tags;
""".strip()


def fetch_overpass_json(overpass_url: str, query: str) -> Dict[str, Any]:
    """
    Calls Overpass API.
    """
    payload = urllib.parse.urlencode({"data": query}).encode("utf-8")
    req = urllib.request.Request(
        overpass_url,
        data=payload,
        method="POST",
        headers={
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "Accept": "application/json, text/plain, */*",
            # Some Overpass instances reject requests without a user-agent.
            "User-Agent": "traveladviser-osm-import/1.0",
        },
    )
    with urllib.request.urlopen(req, timeout=120) as resp:
        raw = resp.read().decode("utf-8", errors="replace")
        return json.loads(raw)


def fetch_overpass_with_fallback(preferred_url: str, query: str) -> Dict[str, Any]:
    urls = [preferred_url] + [u for u in OVERPASS_FALLBACK_URLS if u != preferred_url]
    last_error: Optional[Exception] = None

    for url in urls:
        try:
            return fetch_overpass_json(url, query)
        except HTTPError as e:
            last_error = e
            # 429/5xx/406 are common transient/endpoint-specific failures; try next endpoint.
            if e.code in (406, 429, 500, 502, 503, 504):
                continue
            raise
        except URLError as e:
            last_error = e
            continue

    if last_error:
        raise last_error
    raise RuntimeError("Overpass request failed with unknown error")


def read_cities_csv(path: str) -> List[Dict[str, Any]]:
    rows: List[Dict[str, Any]] = []
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for r in reader:
            rows.append(r)
    return rows


def slugify(value: str) -> str:
    value = value.strip().lower()
    value = re.sub(r"[^a-z0-9]+", "_", value)
    return value.strip("_") or "city"


def main() -> None:
    parser = argparse.ArgumentParser(description="Import real tourist POIs from OSM into `places` + `place_tags` (generates SQL).")
    parser.add_argument("--cities-csv", type=str, default="india_cities.csv", help="CSV with columns: name,state,country,latitude,longitude")
    parser.add_argument("--radius-km", type=float, default=10.0, help="Search radius around each city center")
    parser.add_argument("--max-cities", type=int, default=20, help="Max number of cities to process (start small)")
    parser.add_argument("--pois-per-city", type=int, default=40, help="Hard cap POIs processed per city")
    parser.add_argument("--overpass-url", type=str, default=DEFAULT_OVERPASS_URL, help="Overpass interpreter endpoint")
    parser.add_argument("--out-sql", type=str, default="osm_pois_import.sql", help="Output SQL file path (used when not splitting by city)")
    parser.add_argument("--split-by-city", action="store_true", help="Generate one SQL file per city (easier to import incrementally)")
    parser.add_argument("--out-dir", type=str, default="osm_pois_import_by_city", help="Output directory when --split-by-city is enabled")
    args = parser.parse_args()

    if not os.path.exists(args.cities_csv):
        print(f"Missing --cities-csv: {args.cities_csv}", file=sys.stderr)
        sys.exit(1)

    radius_m = int(args.radius_km * 1000)
    cities = read_cities_csv(args.cities_csv)[: args.max_cities]

    def write_header(out_f) -> None:
        out_f.write("-- Generated by import_osm_pois_to_places.py\n")
        out_f.write("-- Inserts OSM POIs into `places` and maps derived tags into `place_tags`.\n")
        out_f.write("-- Run this SQL in psql.\n\n")

    # Split mode: one SQL per city
    if args.split_by_city:
        os.makedirs(args.out_dir, exist_ok=True)
        for idx, c in enumerate(cities, start=1):
            city_name = (c.get("name") or "").strip()
            state = (c.get("state") or "").strip()
            country = (c.get("country") or "").strip() or "India"
            lat = float(c["latitude"])
            lon = float(c["longitude"])

            if not city_name:
                continue

            file_city = f"{idx:04d}_{slugify(city_name)}.sql"
            out_sql_path = os.path.join(args.out_dir, file_city)

            print(f"[{idx}/{len(cities)}] Querying POIs for {city_name} -> {out_sql_path}")
            query = overpass_query(radius_m, lat, lon)
            try:
                data = fetch_overpass_with_fallback(args.overpass_url, query)
            except Exception as e:
                print(f"  Overpass request failed for {city_name}: {e}", file=sys.stderr)
                continue

            elements = data.get("elements", [])
            processed = 0

            with open(out_sql_path, "w", encoding="utf-8") as out:
                write_header(out)
                out.write(f"-- City: {sql_escape(city_name)}\n")

                for el in elements:
                    if processed >= args.pois_per_city:
                        break

                    el_type = el.get("type")
                    el_id = el.get("id")
                    tags = el.get("tags") or {}

                    coords = osm_center_coords(el)
                    if not coords:
                        continue
                    el_lat, el_lon = coords

                    osm_name = (tags.get("name") or "").strip()
                    if not osm_name:
                        continue

                    category, mapped_tags = map_osm_tags_to_category_and_tags(tags)
                    if not category and not mapped_tags:
                        continue

                    description = (tags.get("description") or "").strip()
                    description_sql = f"'{sql_escape(description)}'" if description else "NULL"

                    category_id_sql = (
                        f"(SELECT id FROM categories WHERE name = '{sql_escape(category)}' LIMIT 1)"
                        if category
                        else "NULL"
                    )

                    best_season = "ALL"
                    price_level = 2
                    source = "osm"
                    source_id = f"{el_type}/{el_id}"

                    out.write(
                        "INSERT INTO places (name, description, category_id, latitude, longitude, location, avg_rating, total_ratings, price_level, best_season, city, state, country, source, source_id)\n"
                    )
                    out.write("VALUES (\n")
                    out.write(f"  '{sql_escape(osm_name)}',\n")
                    out.write(f"  {description_sql},\n")
                    out.write(f"  {category_id_sql},\n")
                    out.write(f"  {el_lat},\n")
                    out.write(f"  {el_lon},\n")
                    out.write(f"  ST_MakePoint({el_lon}, {el_lat})::geography,\n")
                    out.write("  0.0,\n")
                    out.write("  0,\n")
                    out.write(f"  {price_level},\n")
                    out.write(f"  '{best_season}',\n")
                    out.write(f"  '{sql_escape(city_name)}',\n")
                    out.write(f"  '{sql_escape(state)}',\n")
                    out.write(f"  '{sql_escape(country)}',\n")
                    out.write(f"  '{sql_escape(source)}',\n")
                    out.write(f"  '{sql_escape(source_id)}'\n")
                    out.write(")\n")
                    out.write("ON CONFLICT (source, source_id) DO NOTHING;\n")

                    if mapped_tags:
                        tags_in = ", ".join([f"'{sql_escape(t)}'" for t in mapped_tags])
                        out.write(
                            "INSERT INTO place_tags (place_id, tag_id)\n"
                            "SELECT p.id, t.id\n"
                            "FROM places p\n"
                            "JOIN tags t ON TRUE\n"
                            f"WHERE p.source = '{sql_escape(source)}'\n"
                            f"  AND p.source_id = '{sql_escape(source_id)}'\n"
                            f"  AND t.name IN ({tags_in})\n"
                            "ON CONFLICT DO NOTHING;\n"
                        )

                    processed += 1

        print(f"Done. Wrote SQL files into: {args.out_dir}")
        return

    # Combined mode: single SQL file
    out_sql = args.out_sql
    with open(out_sql, "w", encoding="utf-8") as out:
        write_header(out)

        for idx, c in enumerate(cities, start=1):
            city_name = (c.get("name") or "").strip()
            state = (c.get("state") or "").strip()
            country = (c.get("country") or "").strip() or "India"
            lat = float(c["latitude"])
            lon = float(c["longitude"])

            if not city_name:
                continue

            print(f"[{idx}/{len(cities)}] Querying POIs for {city_name}...")
            query = overpass_query(radius_m, lat, lon)

            try:
                data = fetch_overpass_with_fallback(args.overpass_url, query)
            except Exception as e:
                print(f"  Overpass request failed for {city_name}: {e}", file=sys.stderr)
                continue

            elements = data.get("elements", [])
            processed = 0

            out.write(f"\n-- City: {sql_escape(city_name)}\n")
            for el in elements:
                if processed >= args.pois_per_city:
                    break

                el_type = el.get("type")
                el_id = el.get("id")
                tags = el.get("tags") or {}

                coords = osm_center_coords(el)
                if not coords:
                    continue
                el_lat, el_lon = coords

                osm_name = (tags.get("name") or "").strip()
                if not osm_name:
                    continue

                category, mapped_tags = map_osm_tags_to_category_and_tags(tags)
                if not category and not mapped_tags:
                    continue

                description = (tags.get("description") or "").strip()
                description_sql = f"'{sql_escape(description)}'" if description else "NULL"

                category_id_sql = (
                    f"(SELECT id FROM categories WHERE name = '{sql_escape(category)}' LIMIT 1)"
                    if category
                    else "NULL"
                )

                best_season = "ALL"
                price_level = 2
                source = "osm"
                source_id = f"{el_type}/{el_id}"

                out.write(
                    "INSERT INTO places (name, description, category_id, latitude, longitude, location, avg_rating, total_ratings, price_level, best_season, city, state, country, source, source_id)\n"
                )
                out.write("VALUES (\n")
                out.write(f"  '{sql_escape(osm_name)}',\n")
                out.write(f"  {description_sql},\n")
                out.write(f"  {category_id_sql},\n")
                out.write(f"  {el_lat},\n")
                out.write(f"  {el_lon},\n")
                out.write(f"  ST_MakePoint({el_lon}, {el_lat})::geography,\n")
                out.write("  0.0,\n")
                out.write("  0,\n")
                out.write(f"  {price_level},\n")
                out.write(f"  '{best_season}',\n")
                out.write(f"  '{sql_escape(city_name)}',\n")
                out.write(f"  '{sql_escape(state)}',\n")
                out.write(f"  '{sql_escape(country)}',\n")
                out.write(f"  '{sql_escape(source)}',\n")
                out.write(f"  '{sql_escape(source_id)}'\n")
                out.write(")\n")
                out.write("ON CONFLICT (source, source_id) DO NOTHING;\n")

                if mapped_tags:
                    tags_in = ", ".join([f"'{sql_escape(t)}'" for t in mapped_tags])
                    out.write(
                        "INSERT INTO place_tags (place_id, tag_id)\n"
                        "SELECT p.id, t.id\n"
                        "FROM places p\n"
                        "JOIN tags t ON TRUE\n"
                        f"WHERE p.source = '{sql_escape(source)}'\n"
                        f"  AND p.source_id = '{sql_escape(source_id)}'\n"
                        f"  AND t.name IN ({tags_in})\n"
                        "ON CONFLICT DO NOTHING;\n"
                    )

                processed += 1

    print(f"Done. Wrote SQL: {out_sql}")


if __name__ == "__main__":
    main()

