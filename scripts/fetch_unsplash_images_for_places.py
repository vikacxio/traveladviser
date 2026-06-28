import argparse
import sys
import urllib.request


def post_no_body(url: str) -> None:
    req = urllib.request.Request(url, data=b"", method="POST")
    with urllib.request.urlopen(req, timeout=120) as _:
        pass


def main() -> None:
    parser = argparse.ArgumentParser(description="Trigger backend Unsplash image fetch for a list of place IDs.")
    parser.add_argument("--base-url", type=str, default="http://localhost:8080", help="Backend base URL")
    parser.add_argument("--count", type=int, default=3, help="Number of images per place")
    parser.add_argument("--place-ids-file", type=str, required=True, help="Text file with one placeId per line")
    args = parser.parse_args()

    with open(args.place_ids_file, "r", encoding="utf-8") as f:
        ids = [line.strip() for line in f.readlines() if line.strip()]

    for i, pid in enumerate(ids, start=1):
        url = f"{args.base-url}/api/places/{pid}/images/unsplash/fetch?count={args.count}"
        print(f"[{i}/{len(ids)}] Fetching Unsplash images for placeId={pid} ...")
        try:
            post_no_body(url)
        except Exception as e:
            print(f"  Failed for {pid}: {e}", file=sys.stderr)

    print("Done.")


if __name__ == "__main__":
    main()

