#!/usr/bin/env python3
"""Build heartodex.json from the Spanish category index pages.

Heartodex asks not to mass-scrape. This reads only the four Spanish list
pages (peces, aves, insectos, animales salvajes), never each creature's page.

Each card is matched to the id already saved in the app by data-id or, when
the card has none, by the shared image. Those ids live in
tools/heartodex_ids.json so a partida already guardada no cambia de id.
The English name stays in nameEn for search and is not shown in the app.

Price and bait are not on the index, so they are not invented.
Level and shadow are stored only when the card includes them.

    python3 tools/import_heartodex.py
    python3 tools/import_heartodex.py --html-dir /path/to/saved-pages
"""

from __future__ import annotations

import json
import re
import sys
import unicodedata
from html import unescape
from pathlib import Path
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app" / "src" / "main" / "assets" / "catalog" / "heartodex.json"
IDS = ROOT / "tools" / "heartodex_ids.json"
EXTRACTED = "2026-11-07"

PAGES = {
    "Peces": ("https://www.heartodex.com/peces/", "peces.html", "fish"),
    "Pájaros": ("https://www.heartodex.com/aves/", "aves.html", "bird"),
    "Insectos": ("https://www.heartodex.com/insectos/", "insectos.html", "insect"),
    "Fauna": ("https://www.heartodex.com/animales-salvajes/", "fauna.html", "wildlife"),
}

SLOT_KEYS = {
    "dia": "Day",
    "amanecer": "Dawn",
    "atardecer": "Dusk",
    "noche": "Night",
}
WEATHER_KEYS = {
    "arcoiris": "Rainbow",
    "soleado": "Sunny",
    "lluvioso": "Rainy",
    "lluvia": "Rainy",
}


def fold(value: str) -> str:
    text = unicodedata.normalize("NFKD", value).lower()
    text = "".join(ch for ch in text if not unicodedata.combining(ch))
    return re.sub(r"\s+", " ", text).strip()


def slug(prefix: str, name: str) -> str:
    text = fold(name)
    out = []
    for ch in text:
        if ch.isascii() and ch.isalnum():
            out.append(ch)
        elif ch in " -_/":
            out.append("-")
    slug_text = "".join(out)
    while "--" in slug_text:
        slug_text = slug_text.replace("--", "-")
    return f"{prefix}-{slug_text.strip('-')}"


def fetch(url: str) -> str:
    request = Request(
        url,
        headers={
            "User-Agent": "GuiaHeartopia/0.4.1 (four index pages, once)",
            "Accept": "text/html",
            "Accept-Language": "es",
        },
    )
    with urlopen(request, timeout=40) as response:
        body = response.read().decode("utf-8", "replace")
    if "Just a moment" in body or "dex-card" not in body:
        raise RuntimeError(f"La página índice no llegó completa: {url}")
    return body


def parse_cards(html: str) -> list[dict]:
    cards = []
    seen = set()
    for match in re.finditer(r"<a\b([^>]*\bdata-name=\"[^\"]*\"[^>]*)>", html):
        attrs = match.group(1)
        if "dex-card" not in attrs and "data-href" not in attrs:
            continue

        def attr(name: str) -> str:
            found = re.search(rf'\b{name}="([^"]*)"', attrs)
            return unescape(found.group(1)).strip() if found else ""

        href = attr("href") or attr("data-href")
        if not href or href in seen:
            continue
        seen.add(href)
        start = match.end()
        nxt = html.find('class="dex-card', start)
        chunk = html[start:nxt if nxt != -1 else start + 8000]
        image = re.search(r"<img\b[^>]*>", chunk)
        alt = src = ""
        if image:
            tag = image.group(0)
            alt_match = re.search(r'alt="([^"]*)"', tag)
            src_match = re.search(r'src="([^"]*)"', tag)
            alt = unescape(alt_match.group(1)).strip() if alt_match else ""
            src = src_match.group(1) if src_match else ""
        cards.append(
            {
                "heartodexId": attr("data-id"),
                "name": attr("data-name"),
                "alt": alt,
                "image": src,
                "level": attr("data-level"),
                "shadow": attr("data-shadow"),
                "weather": attr("data-weather"),
                "schedule": attr("data-schedule"),
                "location": attr("data-location"),
                "categories": attr("data-categories"),
                "href": href,
            }
        )
    return cards


def split_list(value: str) -> list[str]:
    return [part.strip() for part in value.split(",") if part.strip()]


def map_keys(value: str, table: dict[str, str]) -> list[str]:
    keys = []
    for part in split_list(value):
        key = table.get(fold(part))
        if key and key not in keys:
            keys.append(key)
    return keys


def level_of(raw: str) -> int | None:
    if not raw.strip().isdigit():
        return None
    number = int(raw)
    return number if number > 0 else None


def load_pages(html_dir: Path | None) -> dict[str, str]:
    pages = {}
    for category, (url, filename, _prefix) in PAGES.items():
        if html_dir is not None:
            pages[category] = (html_dir / filename).read_text(encoding="utf-8")
        else:
            pages[category] = fetch(url)
    return pages


def build(html_by_category: dict[str, str]) -> tuple[dict, dict]:
    registry = json.loads(IDS.read_text(encoding="utf-8"))
    by_image = {row["image"]: row for row in registry if row.get("image")}
    by_dex = {row["heartodexId"]: row for row in registry if row.get("heartodexId")}
    used = set()
    entries = []
    seen_ids = {row["id"] for row in registry}
    report = {category: {"withName": 0, "missing": []} for category in PAGES}

    for category, (_url, _filename, prefix) in PAGES.items():
        for card in parse_cards(html_by_category[category]):
            known = by_image.get(card["image"]) or by_dex.get(card["heartodexId"])
            if known and known["id"] not in {item["id"] for item in entries}:
                entry_id = known["id"]
                name_en = known["nameEn"]
                used.add(entry_id)
            else:
                tail = card["href"].rstrip("/").split("/")[-1] or card["name"]
                entry_id = slug(prefix, tail)
                base = entry_id
                number = 2
                while entry_id in seen_ids:
                    entry_id = f"{base}-{number}"
                    number += 1
                seen_ids.add(entry_id)
                name_en = ""
            display = card["alt"] or card["name"]
            if display:
                report[category]["withName"] += 1
            else:
                report[category]["missing"].append(name_en or card["href"])
            event = card["categories"]
            if fold(event) in {"", "comun"}:
                event = None
            entry = {
                "id": entry_id,
                "category": category,
                "name": display,
                "nameEn": name_en,
                "location": split_list(card["location"]),
                "schedule": map_keys(card["schedule"], SLOT_KEYS),
                "weather": map_keys(card["weather"], WEATHER_KEYS),
                "event": event,
                "source": "heartodex",
                "sourceUrl": PAGES[category][0],
                "extractedOn": EXTRACTED,
            }
            level = level_of(card["level"])
            if level is not None:
                entry["level"] = level
            if card["shadow"]:
                entry["shadow"] = card["shadow"]
            entries.append(entry)

    for row in registry:
        if row["id"] not in used:
            report[row["category"]]["missing"].append(row["nameEn"])

    payload = {
        "source": "https://www.heartodex.com/",
        "extractedOn": EXTRACTED,
        "terms": "https://www.heartodex.com/en/terms/",
        "note": (
            "Páginas índice en español (peces, aves, insectos, animales salvajes), "
            "sin abrir cada ficha. El id de la app no cambia: se empareja por data-id "
            "o por la imagen. El nombre en inglés queda en nameEn para la búsqueda. "
            "Nivel y sombra solo si la tarjeta los trae. Precio y cebo no están en el índice."
        ),
        "entries": entries,
    }
    return payload, report


def main() -> None:
    html_dir = None
    if "--html-dir" in sys.argv:
        html_dir = Path(sys.argv[sys.argv.index("--html-dir") + 1])
    payload, report = build(load_pages(html_dir))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(OUT)
    for category, info in report.items():
        missing = info["missing"]
        print(f"{category}: {info['withName']} con nombre en español")
        if missing:
            print("  sin nombre en español:")
            for name in missing:
                print(f"  - {name}")
        else:
            print("  sin nombre en español: ninguna")
    print(f"total: {len(payload['entries'])}")


if __name__ == "__main__":
    main()
