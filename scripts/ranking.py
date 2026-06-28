import argparse
import csv
import json
import os
import re
import sys
import urllib.parse
import urllib.request
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib.error import HTTPError, URLError
from typing import Any, Dict, List, Optional, Tuple

DEFAULT_OVERPASS_URL = "https://overpass-api.de/api/interpreter"

OVERPASS_FALLBACK_URLS = [
    DEFAULT_OVERPASS_URL,
    "https://lz4.overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
]

WIKI_CACHE: Dict[str, Optional[str]] = {}

# How many candidate POIs to fetch Wikipedia for (before final top-N cut).
# Set to pois_per_city * 2 dynamically in process_city.
WIKI_CANDIDATE_MULTIPLIER = 2

# Max parallel Wikipedia threads. Keep modest to avoid rate-limiting.
WIKI_WORKERS = 8


# ---------------- REGION ----------------
def get_region(state: str, lat: float) -> str:
    state = (state or "").lower()
    if state in ["himachal pradesh", "uttarakhand", "jammu and kashmir", "ladakh"]:
        return "HIMALAYAN"
    if state in ["goa", "kerala", "tamil nadu", "andhra pradesh", "odisha",
                 "west bengal", "maharashtra", "karnataka", "gujarat"]:
        return "COASTAL"
    if state in ["rajasthan"]:
        return "DESERT"
    if lat > 23:
        return "NORTH"
    return "SOUTH"


# ---------------- SEASON ----------------
def infer_best_season(category: Optional[str], state: str, lat: float, tags: Dict[str, Any]) -> str:
    region = get_region(state, lat)
    category = (category or "").lower()

    if category == "beach":
        return "NOV-FEB" if region == "COASTAL" else "OCT-MAR"
    elif category == "mountain":
        return "APR-JUN, SEP-OCT" if region == "HIMALAYAN" else "OCT-MAR"
    elif category == "waterfall":
        return "JUL-OCT" if region in ["SOUTH", "COASTAL"] else "AUG-OCT"
    elif category == "lake":
        return "MAY-OCT" if region == "HIMALAYAN" else "OCT-MAR"
    elif region == "DESERT":
        return "NOV-FEB"
    elif category == "temple":
        name = (tags.get("name") or "").lower()
        if any(x in name for x in ["kedarnath", "badrinath", "amarnath"]):
            return "MAY-OCT"
        return "ALL"
    elif category in ["fort", "historical"]:
        return "OCT-MAR" if region in ["NORTH", "DESERT"] else "NOV-FEB"
    elif category in ["park", "garden"]:
        return "FEB-APR, AUG-NOV" if region == "NORTH" else "NOV-FEB"
    elif category == "wildlife":
        return "NOV-APR"
    elif category == "adventure":
        return "APR-JUN, SEP-OCT" if region == "HIMALAYAN" else "OCT-MAR"
    return "OCT-MAR"


# ---------------- PRICE ----------------
def infer_price_level(category: Optional[str], tags: Dict[str, Any]) -> int:
    category = (category or "").lower()
    name = (tags.get("name") or "").lower()
    tourism = (tags.get("tourism") or "").lower()
    fee = (tags.get("fee") or "").lower()

    score = 0

    if category in ["temple", "park", "garden"]:
        score += 1
    elif category in ["fort", "historical", "museum"]:
        score += 2
    elif category in ["wildlife"]:
        score += 3
    elif category in ["adventure"]:
        score += 4
    elif category in ["beach", "mountain", "waterfall", "lake"]:
        score += 1

    if tourism == "theme_park":
        score += 5
    elif tourism in ["zoo", "aquarium"]:
        score += 4
    elif tourism in ["museum", "gallery"]:
        score += 2
    elif tourism == "attraction":
        score += 2

    if any(x in name for x in ["resort", "luxury", "premium"]):
        score += 5
    if any(x in name for x in ["mall", "shopping", "market"]):
        score += 2
    if any(x in name for x in ["national park", "sanctuary"]):
        score += 3
    if any(x in name for x in ["fort", "museum", "temple"]):
        score += 1
    if fee == "yes":
        score += 2

    if score <= 2:   return 1
    elif score <= 4: return 2
    elif score <= 6: return 3
    elif score <= 9: return 4
    else:            return 5


# ---------------- WIKIPEDIA ----------------
VERBOSE = False


def fetch_wikipedia_summary(name: str, tags: Dict[str, Any]) -> Optional[str]:
    search_name = (tags.get("name:en") or name).strip()
    wiki_tag = tags.get("wikipedia")

    key = (wiki_tag or search_name).lower().strip()

    # FIX 1: Return immediately from cache — no sleep on cache hits.
    if key in WIKI_CACHE:
        return WIKI_CACHE[key]

    title = None
    lang = "en"

    if wiki_tag:
        if ":" in wiki_tag:
            lang, title = wiki_tag.split(":", 1)
        else:
            title = wiki_tag

    if not title:
        title = search_name

    title = title.replace(" ", "_")

    try:
        url = f"https://{lang}.wikipedia.org/api/rest_v1/page/summary/{urllib.parse.quote(title)}"
        req = urllib.request.Request(url, headers={"User-Agent": "travelapp/1.0"})

        # FIX 2: Only sleep on actual network calls (not cache hits above).
        time.sleep(0.3)

        data = json.loads(urllib.request.urlopen(req, timeout=10).read().decode())
        extract = data.get("extract")
        WIKI_CACHE[key] = extract
        return extract

    except Exception:
        WIKI_CACHE[key] = None
        return None


def fetch_wikipedia_batch(candidates: List[Tuple[str, Dict[str, Any]]], workers: int = WIKI_WORKERS) -> Dict[str, Optional[str]]:
    """
    FIX 3: Fetch Wikipedia summaries in parallel using a thread pool.
    Returns a dict mapping the lookup-key to the summary text (or None).
    """
    results: Dict[str, Optional[str]] = {}

    def _fetch(item):
        name, tags = item
        return name, fetch_wikipedia_summary(name, tags)

    with ThreadPoolExecutor(max_workers=workers) as pool:
        futures = {pool.submit(_fetch, item): item for item in candidates}
        for future in as_completed(futures):
            name, summary = future.result()
            results[name] = summary

    return results


# ---------------- CATEGORY + TAGS ----------------
def map_osm_tags_to_category_and_tags(tags: Dict[str, Any]) -> Tuple[Optional[str], List[str]]:
    tourism  = (tags.get("tourism")  or "").lower()
    amenity  = (tags.get("amenity")  or "").lower()
    historic = (tags.get("historic") or "").lower()
    waterway = (tags.get("waterway") or "").lower()
    natural  = (tags.get("natural")  or "").lower()
    leisure  = (tags.get("leisure")  or "").lower()
    water    = (tags.get("water")    or "").lower()
    shop     = (tags.get("shop")     or "").lower()

    category: Optional[str] = None
    out_tags: List[str] = []

    def add(t: str) -> None:
        if t not in out_tags:
            out_tags.append(t)

    if tourism == "beach" or natural == "coastline":
        category = "Beach"
        add("beach"); add("water_spot"); add("outdoor"); add("photography")
    elif tourism in ("museum", "gallery"):
        category = "Museum"
        add("museum"); add("indoor"); add("historical"); add("photography")
    elif tourism in ("park", "picnic_site") or leisure == "park":
        category = "Park"
        add("outdoor"); add("family_friendly"); add("photography")
    elif tourism == "garden" or leisure in ("garden", "botanical_garden"):
        category = "Garden"
        add("outdoor"); add("photography"); add("romantic")
    elif tourism in ("zoo", "aquarium"):
        category = "Wildlife"
        add("wildlife"); add("nature_lover"); add("outdoor"); add("photography")
    elif amenity == "place_of_worship":
        category = "Temple"
        add("spiritual"); add("historical"); add("photography")
    elif historic in ("castle", "fortress", "fort"):
        category = "Fort"
        add("historical"); add("photography"); add("cool_place")
    elif historic in ("monument", "memorial", "ruins", "archaeological_site"):
        category = "Historical"
        add("historical"); add("photography"); add("cool_place")
    elif waterway == "waterfall":
        category = "Waterfall"
        add("water_spot"); add("outdoor"); add("nature_lover"); add("photography")
    elif natural == "water" and water in ("lake", "lagoon", "reservoir"):
        category = "Lake"
        add("water_spot"); add("outdoor"); add("nature_lover"); add("photography")
    elif tourism in ("viewpoint", "attraction", "theme_park"):
        category = "Adventure"
        add("adventure"); add("outdoor"); add("photography")
    elif natural in ("peak", "volcano"):
        category = "Mountain"
        add("adventure"); add("nature_lover"); add("outdoor"); add("photography"); add("hiking")
    elif shop or tourism == "mall":
        category = "Shopping"
        add("indoor"); add("family_friendly")

    return category, out_tags


# ---------------- OVERPASS QUERY ----------------
def overpass_query(radius_m: int, lat: float, lon: float) -> str:
    return f"""
[out:json][timeout:90];
(
  node["tourism"~"museum|gallery|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});
  way["tourism"~"museum|gallery|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});
  relation["tourism"~"museum|gallery|park|picnic_site|garden|beach|zoo|aquarium|viewpoint|attraction|theme_park"](around:{radius_m},{lat},{lon});

  node["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});
  way["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});
  relation["amenity"="place_of_worship"](around:{radius_m},{lat},{lon});

  node["historic"~"castle|fortress|fort|monument|memorial|ruins|archaeological_site"](around:{radius_m},{lat},{lon});
  way["historic"~"castle|fortress|fort|monument|memorial|ruins|archaeological_site"](around:{radius_m},{lat},{lon});
  relation["historic"~"castle|fortress|fort|monument|memorial|ruins|archaeological_site"](around:{radius_m},{lat},{lon});

  node["waterway"="waterfall"](around:{radius_m},{lat},{lon});
  way["waterway"="waterfall"](around:{radius_m},{lat},{lon});

  node["natural"="water"]["water"~"lake|lagoon|reservoir"](around:{radius_m},{lat},{lon});
  way["natural"="water"]["water"~"lake|lagoon|reservoir"](around:{radius_m},{lat},{lon});

  node["natural"~"peak|volcano"](around:{radius_m},{lat},{lon});
  way["natural"~"peak|volcano"](around:{radius_m},{lat},{lon});

  node["leisure"~"park|garden|botanical_garden"](around:{radius_m},{lat},{lon});
  way["leisure"~"park|garden|botanical_garden"](around:{radius_m},{lat},{lon});
);
out center tags;
""".strip()


# ---------------- OVERPASS FETCH ----------------
def fetch_overpass(query: str) -> Dict[str, Any]:
    payload = urllib.parse.urlencode({"data": query}).encode("utf-8")
    headers = {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        "Accept": "application/json",
        "User-Agent": "travelapp-osm-import/1.0",
    }
    last_error: Optional[Exception] = None

    for url in OVERPASS_FALLBACK_URLS:
        try:
            req = urllib.request.Request(url, data=payload, method="POST", headers=headers)
            with urllib.request.urlopen(req, timeout=120) as resp:
                return json.loads(resp.read().decode("utf-8", errors="replace"))
        except (HTTPError, URLError) as e:
            code = e.code if isinstance(e, HTTPError) else None
            print(f"  [warn] {url} failed ({code or e}), trying next...")
            last_error = e
            continue

    raise RuntimeError(f"All Overpass endpoints failed. Last error: {last_error}")


# ---------------- SCORING ----------------
def score_poi(tags: Dict[str, Any], category: Optional[str], wiki_desc: Optional[str]) -> int:
    score = 0
    name = (tags.get("name") or "").lower()

    if tags.get("wikidata"):
        score += 8
    if tags.get("wikipedia"):
        score += 5
    if wiki_desc:
        score += 4
        if len(wiki_desc) > 500:
            score += 2
        if len(wiki_desc) > 1500:
            score += 1

    if tags.get("name:en"):
        score += 2
    if tags.get("website") or tags.get("contact:website"):
        score += 2
    if tags.get("image") or tags.get("wikimedia_commons"):
        score += 2
    if tags.get("opening_hours"):
        score += 1
    if tags.get("phone") or tags.get("contact:phone"):
        score += 1

    if category in ("Fort", "Mountain", "Beach", "Waterfall"):
        score += 8
    elif category in ("Museum", "Lake", "Wildlife"):
        score += 6
    elif category in ("Temple", "Historical", "Adventure"):
        score += 4
    elif category in ("Park", "Garden"):
        score += 2

    tourism = (tags.get("tourism") or "").lower()
    if tourism == "attraction":
        score += 4
    elif tourism in ("museum", "gallery", "zoo", "aquarium", "theme_park"):
        score += 3
    elif tourism in ("viewpoint", "beach"):
        score += 2

    high_prestige = ["national", "heritage", "world heritage", "palace",
                     "fort", "mahal", "caves", "falls", "wildlife", "sanctuary"]
    medium_prestige = ["temple", "museum", "garden", "lake", "peak",
                       "reserve", "monument", "memorial"]

    if any(w in name for w in high_prestige):
        score += 4
    elif any(w in name for w in medium_prestige):
        score += 2

    return score


# ---------------- DEDUP ----------------
def is_duplicate(existing: List[Tuple[str, float, float]], name: str, lat: float, lon: float) -> bool:
    name_l = name.lower()
    for ename, elat, elon in existing:
        if name_l in ename or ename in name_l:
            if abs(elat - lat) < 0.001 and abs(elon - lon) < 0.001:
                return True
    return False


# ---------------- HELPERS ----------------
def sql_escape(value: str) -> str:
    return value.replace("'", "''")


def osm_center_coords(el: Dict[str, Any]) -> Optional[Tuple[float, float]]:
    if "lat" in el and "lon" in el:
        return float(el["lat"]), float(el["lon"])
    center = el.get("center")
    if center and "lat" in center:
        return float(center["lat"]), float(center["lon"])
    return None


def slugify(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", value.strip().lower()).strip("_") or "city"


def main() -> None:
    parser = argparse.ArgumentParser(description="Import OSM tourist POIs → SQL for places + place_tags.")
    parser.add_argument("--cities-csv", required=True)
    parser.add_argument("--max-cities", type=int, default=5)
    parser.add_argument("--pois-per-city", type=int, default=25)
    parser.add_argument("--radius-km", type=float, default=8.0)
    parser.add_argument("--split-by-city", action="store_true")
    parser.add_argument("--out-dir", type=str, default="osm_output")
    parser.add_argument("--out-sql", type=str, default="osm_pois.sql")
    parser.add_argument("--verbose", action="store_true")
    parser.add_argument("--wiki-workers", type=int, default=WIKI_WORKERS,
                        help="Parallel threads for Wikipedia fetches (default: 8)")
    args = parser.parse_args()

    global VERBOSE
    VERBOSE = args.verbose

    radius_m = int(args.radius_km * 1000)

    try:
        with open(args.cities_csv, encoding="utf-8") as f:
            cities = list(csv.DictReader(f))[:args.max_cities]
    except Exception as e:
        print(f"[FATAL] Failed to read CSV: {e}")
        sys.exit(1)

    if not cities:
        print("[FATAL] No cities found in CSV")
        sys.exit(1)

    if args.split_by_city:
        os.makedirs(args.out_dir, exist_ok=True)

    def process_city(city: Dict[str, Any], out):
        city_name = (city.get("name") or "").strip()
        state = (city.get("state") or "").strip()
        country = (city.get("country") or "India").strip()

        if not city_name:
            print("[WARN] Skipping city with empty name")
            return

        lat = float(city["latitude"])
        lon = float(city["longitude"])

        print(f"\n=== Processing {city_name} ===")

        query = overpass_query(radius_m, lat, lon)
        data = fetch_overpass(query)
        elements = data.get("elements", [])

        if not elements:
            print(f"[WARN] No OSM data found for {city_name}")
            return

        # ----------------------------------------------------------------
        # PHASE 1: Score every element WITHOUT Wikipedia (fast, no I/O).
        # ----------------------------------------------------------------
        phase1 = []
        for el in elements:
            tags = el.get("tags") or {}
            coords = osm_center_coords(el)
            if not coords:
                continue

            name = (tags.get("name:en") or tags.get("name") or "").strip()
            if not name:
                continue

            category, mapped_tags = map_osm_tags_to_category_and_tags(tags)
            if not category:
                continue

            score = score_poi(tags, category, wiki_desc=None)
            phase1.append((score, el, category, mapped_tags))

        phase1.sort(reverse=True, key=lambda x: x[0])

        # ----------------------------------------------------------------
        # PHASE 2: Fetch Wikipedia only for the top candidates (parallel).
        # ----------------------------------------------------------------
        candidate_count = args.pois_per_city * WIKI_CANDIDATE_MULTIPLIER
        top_candidates = phase1[:candidate_count]

        wiki_inputs = []
        for _, el, _, _ in top_candidates:
            tags = el.get("tags") or {}
            name = (tags.get("name:en") or tags.get("name") or "").strip()
            wiki_inputs.append((name, tags))

        print(f"  Fetching Wikipedia for {len(wiki_inputs)} candidates "
              f"(parallel, {args.wiki_workers} workers)…")
        wiki_results = fetch_wikipedia_batch(wiki_inputs, workers=args.wiki_workers)

        # ----------------------------------------------------------------
        # PHASE 3: Re-score with Wikipedia, then write top-N.
        # ----------------------------------------------------------------
        scored = []
        for score_p1, el, category, mapped_tags in top_candidates:
            tags = el.get("tags") or {}
            name = (tags.get("name:en") or tags.get("name") or "").strip()
            desc = wiki_results.get(name)
            osm_desc = (tags.get("description") or "").strip() or None
            description = desc or osm_desc
            full_score = score_poi(tags, category, desc)
            scored.append((full_score, el, category, mapped_tags, description))

        scored.sort(reverse=True, key=lambda x: x[0])

        seen = []
        processed = 0

        for score, el, category, mapped_tags, description in scored:
            if processed >= args.pois_per_city:
                break

            tags = el.get("tags") or {}
            coords = osm_center_coords(el)
            if not coords:
                continue

            el_lat, el_lon = coords
            name = (tags.get("name:en") or tags.get("name") or "").strip()

            if is_duplicate(seen, name, el_lat, el_lon):
                continue

            seen.append((name.lower(), el_lat, el_lon))

            source_id = f"{el.get('type')}/{el.get('id')}"

            if VERBOSE:
                print(f"[WRITE] {name} score={score} desc={'YES' if description else 'NO'}")

            description_sql = f"'{sql_escape(description)}'" if description else "NULL"
            category_id_sql = f"(SELECT id FROM categories WHERE name='{sql_escape(category)}' LIMIT 1)"
            best_season = infer_best_season(category, state, el_lat, tags)
            price_level = infer_price_level(category, tags)

            out.write(
                "INSERT INTO places (name, description, category_id, latitude, longitude, location,"
                "avg_rating, total_ratings, price_level, best_season, city, state, country, source, source_id)\n"
                "VALUES (\n"
                f"'{sql_escape(name)}',\n"
                f"{description_sql},\n"
                f"{category_id_sql},\n"
                f"{el_lat},\n{el_lon},\n"
                f"ST_MakePoint({el_lon}, {el_lat})::geography,\n"
                "0.0,0,\n"
                f"{price_level},\n"
                f"'{best_season}',\n"
                f"'{sql_escape(city_name)}',\n"
                f"'{sql_escape(state)}',\n"
                f"'{sql_escape(country)}',\n"
                "'osm',\n"
                f"'{sql_escape(source_id)}'\n"
                ")\nON CONFLICT DO NOTHING;\n\n"
            )

            processed += 1

        print(f"=== {city_name}: {processed} POIs written ===")

    try:
        if args.split_by_city:
            for i, city in enumerate(cities, 1):
                name = city.get("name", "city")
                file_path = os.path.join(args.out_dir, f"{i:03d}_{slugify(name)}.sql")
                print(f"\n[{i}/{len(cities)}] {name}")
                with open(file_path, "w", encoding="utf-8") as out:
                    out.write(f"-- {name}\n\n")
                    process_city(city, out)
        else:
            with open(args.out_sql, "w", encoding="utf-8") as out:
                for i, city in enumerate(cities, 1):
                    print(f"\n[{i}/{len(cities)}] {city.get('name')}")
                    process_city(city, out)

    except KeyboardInterrupt:
        print("\n[STOPPED] Interrupted by user")
    except Exception as e:
        print(f"[FATAL ERROR] {e}")
        raise


if __name__ == "__main__":
    main()
