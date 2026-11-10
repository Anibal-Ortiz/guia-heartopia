#!/usr/bin/env python3
"""Arma la carpeta que se sube a GitHub Pages.

Lee el catálogo y el clima del proyecto y escribe un paquete
con manifest.json, tamaños y SHA-256. El formato es el que lee la app.
Por defecto no copia imágenes: la app dibuja un ícono por categoría.
Con --con-imagenes sí incluye los WebP de catalog/images.

Por defecto suma 1 a la contentVersion ya generada (o a la del APK).
Con --inicial deja la versión 1, igual al contenido que ya trae la app.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
GRADLE = ROOT / "app" / "build.gradle.kts"


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def version_code() -> int:
    for line in GRADLE.read_text(encoding="utf-8").splitlines():
        if "versionCode" in line and "=" in line:
            return int(line.split("=", 1)[1].strip())
    raise SystemExit("No encontré versionCode en app/build.gradle.kts")


def read_version(path: Path) -> int | None:
    if not path.is_file():
        return None
    try:
        value = json.loads(path.read_text(encoding="utf-8")).get("contentVersion")
    except json.JSONDecodeError:
        return None
    return value if isinstance(value, int) else None


def describe(entry: dict) -> str:
    locations = [str(item).strip() for item in entry.get("location") or [] if str(item).strip()]
    where = ", ".join(locations) if locations else "varios puntos del mapa"
    kind = {
        "Peces": "Pez de la guía",
        "Pájaros": "Ave de la guía",
        "Insectos": "Insecto de la guía",
        "Fauna": "Animal de la guía",
    }.get(entry.get("category"), "Criatura de la guía")
    return f"{kind}. Se anota en {where}. Horario y clima van en los datos, no en este texto."


def creatures_payload(raw: bytes) -> bytes:
    data = json.loads(raw)
    data.pop("imageNote", None)
    data.pop("imageBundledOn", None)
    data.pop("terms", None)
    data["note"] = "Fichas con datos de aparición. El texto breve de cada una está escrito para esta guía."
    for entry in data.get("entries", []):
        entry["description"] = describe(entry)
    return (json.dumps(data, ensure_ascii=False, indent=2) + "\n").encode("utf-8")


def collect(source: Path, include_images: bool) -> list[tuple[str, bytes]]:
    creatures = source / "catalog" / "creatures.json"
    heartodex = source / "catalog" / "heartodex.json"
    if creatures.is_file():
        raw_creatures = creatures.read_bytes()
    elif heartodex.is_file():
        raw_creatures = heartodex.read_bytes()
    else:
        raise SystemExit(f"No está el catálogo en {source / 'catalog'}")

    calendar = sorted((source / "calendar").glob("clima-*.json"))
    if not calendar:
        raise SystemExit(f"No hay clima en {source / 'calendar'}")

    files: list[tuple[str, bytes]] = [("catalog/creatures.json", creatures_payload(raw_creatures))]
    creditos = source / "catalog" / "creditos.json"
    if creditos.is_file():
        files.append(("catalog/creditos.json", creditos.read_bytes()))
    files.extend((f"calendar/{item.name}", item.read_bytes()) for item in calendar)
    if include_images:
        images = sorted((source / "catalog" / "images").glob("*.webp"))
        if not images:
            raise SystemExit(f"No hay imágenes WebP en {source / 'catalog' / 'images'}")
        files.extend((f"catalog/images/{item.name}", item.read_bytes()) for item in images)
    return files


def choose_version(salida: Path, source: Path, explicit: int | None, inicial: bool) -> int:
    if inicial:
        return 1
    if explicit is not None:
        if explicit < 1:
            raise SystemExit("contentVersion tiene que ser 1 o más")
        return explicit
    previous = read_version(salida / "manifest.json")
    if previous is None:
        previous = read_version(source / "content" / "manifest.json")
    if previous is None:
        previous = read_version(ASSETS / "content" / "manifest.json")
    return (previous or 0) + 1


def write_tree(folder: Path, manifest_text: str, files: list[tuple[str, bytes]]) -> None:
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "manifest.json").write_text(manifest_text, encoding="utf-8")
    for relative, data in files:
        dest = folder / relative
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)


def main() -> None:
    parser = argparse.ArgumentParser(description="Genera el paquete de contenido para GitHub Pages.")
    parser.add_argument("--salida", default=str(ROOT / "build" / "contenido-publicado"))
    parser.add_argument("--desde", default=str(ASSETS), help="Carpeta con catalog/ y calendar/")
    parser.add_argument("--version", type=int, default=None, help="contentVersion fija")
    parser.add_argument("--inicial", action="store_true", help="Publica contentVersion 1")
    parser.add_argument(
        "--con-imagenes",
        action="store_true",
        help="Incluye catalog/images. Por defecto el paquete no lleva arte.",
    )
    parser.add_argument("--fecha", default=date.today().isoformat())
    args = parser.parse_args()

    source = Path(args.desde)
    salida = Path(args.salida)
    files = collect(source, args.con_imagenes)
    version = choose_version(salida, source, args.version, args.inicial)
    manifest = {
        "contentVersion": version,
        "minAppVersionCode": version_code(),
        "publishedOn": args.fecha,
        "files": [
            {"path": relative, "size": len(data), "sha256": sha256(data)}
            for relative, data in files
        ],
    }
    text = json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
    if salida.exists():
        shutil.rmtree(salida)
    write_tree(salida, text, files)
    # La app 0.6.0 descarga desde la carpeta content/ del sitio.
    write_tree(salida / "content", text, files)
    print(f"Listo: {salida}")
    print(f"contentVersion {version}")
    print(f"archivos {len(files)}")
    print(f"minAppVersionCode {manifest['minAppVersionCode']}")


if __name__ == "__main__":
    main()
