import argparse
import csv
import io
import os
import zipfile
from dataclasses import dataclass
from typing import Dict, List, Tuple
from urllib.request import urlopen


GEONAMES_CITIES_URL = "https://download.geonames.org/export/dump/cities15000.zip"
GEONAMES_ADMIN1_URL = "https://download.geonames.org/export/dump/admin1CodesASCII.txt"


@dataclass(frozen=True)
class CityRow:
    name: str
    state: str
    country: str
    latitude: float
    longitude: float
    population: int


def download_bytes(url: str) -> bytes:
    with urlopen(url) as resp:
        return resp.read()


def parse_admin1_codes(admin1_text: str) -> Dict[str, str]:
    """
    admin1CodesASCII.txt format (tab-delimited):
      Code, Name, AsciiName, GeonameID
    where Code typically looks like "IN.01"
    """
    mapping: Dict[str, str] = {}
    for line in admin1_text.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        code = parts[0].strip()
        name = parts[1].strip()
        if code and name:
            mapping[code] = name
    return mapping


def parse_geonames_cities(cities_lines: io.TextIOBase, admin1_map: Dict[str, str], limit: int) -> List[CityRow]:
    """
    cities15000.txt columns (tab-delimited):
      0 geonameid
      1 name
      2 asciiname
      3 alternatenames
      4 latitude
      5 longitude
      6 feature class
      7 feature code
      8 country code
      9 cc2
      10 admin1 code
      11 admin2 code
      12 admin3 code
      13 admin4 code
      14 population
      15 elevation
      16 dem
      17 timezone
      18 modification date
    """
    best_by_dedup_key: Dict[Tuple[str, float, float], CityRow] = {}

    for raw in cities_lines:
        line = raw.rstrip("\n")
        if not line:
            continue
        parts = line.split("\t")
        if len(parts) < 15:
            continue

        name = parts[1].strip()
        feature_class = parts[6].strip()
        country_code = parts[8].strip()
        if country_code != "IN":
            continue
        # Only populated places (cities/towns/villages). GeoNames uses feature class 'P' for Populated places.
        if feature_class != "P":
            continue

        try:
            latitude = float(parts[4])
            longitude = float(parts[5])
        except ValueError:
            continue

        admin1_code = parts[10].strip()
        state = admin1_map.get(admin1_code, "")

        try:
            population = int(float(parts[14]) if parts[14] else 0)
        except ValueError:
            population = 0

        # Deduplicate: same name + rounded coords. Keep the highest population record.
        dedup_key = (name, round(latitude, 4), round(longitude, 4))
        existing = best_by_dedup_key.get(dedup_key)
        row = CityRow(
            name=name,
            state=state,
            country="India",
            latitude=latitude,
            longitude=longitude,
            population=population,
        )
        if existing is None or row.population > existing.population:
            best_by_dedup_key[dedup_key] = row

    # Sort by population desc, then by name asc for stability.
    rows = sorted(best_by_dedup_key.values(), key=lambda r: (-r.population, r.name))
    return rows[:limit]


def main() -> None:
    parser = argparse.ArgumentParser(description="Build India cities CSV from GeoNames (accurate source).")
    parser.add_argument("--limit", type=int, default=1000, help="Number of cities to output (top by population).")
    parser.add_argument("--out-csv", type=str, default="india_cities.csv", help="Output CSV path.")
    args = parser.parse_args()

    out_csv = args.out_csv
    if os.path.dirname(out_csv):
        os.makedirs(os.path.dirname(out_csv), exist_ok=True)

    print(f"Downloading GeoNames data...")
    cities_zip_bytes = download_bytes(GEONAMES_CITIES_URL)
    admin1_text = download_bytes(GEONAMES_ADMIN1_URL).decode("utf-8", errors="replace")

    print("Parsing admin1 code -> state name...")
    admin1_map = parse_admin1_codes(admin1_text)

    print("Extracting cities15000.zip...")
    with zipfile.ZipFile(io.BytesIO(cities_zip_bytes)) as zf:
        # Usually contains a single .txt like cities15000.txt
        txt_names = [n for n in zf.namelist() if n.endswith(".txt")]
        if not txt_names:
            raise RuntimeError("cities15000.zip did not contain a .txt file")
        txt_name = txt_names[0]
        with zf.open(txt_name) as f:
            # GeoNames files are ISO-8859-1-ish in practice; fall back safely.
            text_stream = io.TextIOWrapper(f, encoding="utf-8", errors="replace")
            print("Filtering India populated places...")
            rows = parse_geonames_cities(text_stream, admin1_map, args.limit)

    print(f"Writing CSV: {out_csv} ({len(rows)} rows)")
    with open(out_csv, "w", newline="", encoding="utf-8") as out_f:
        writer = csv.writer(out_f)
        writer.writerow(["name", "state", "country", "latitude", "longitude"])
        for r in rows:
            writer.writerow([r.name, r.state, r.country, f"{r.latitude}", f"{r.longitude}"])

    print("Done.")


if __name__ == "__main__":
    main()

