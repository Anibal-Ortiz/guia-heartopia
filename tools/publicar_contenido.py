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


def is_filler(text: str) -> bool:
    value = text.strip()
    return "de la guía" in value and "Horario y clima van en los datos" in value


def creatures_payload(raw: bytes) -> bytes:
    data = json.loads(raw)
    data.pop("imageNote", None)
    data.pop("imageBundledOn", None)
    data.pop("source", None)
    data.pop("terms", None)
    if "extractedOn" in data:
        data["extractedOn"] = ""
    data["note"] = (
        "Fichas con datos de aparición. Ubicación, horario, clima y nivel van en los datos de cada ficha."
    )
    for entry in data.get("entries", []):
        for key in ("source", "sourceUrl", "extractedOn"):
            if key in entry:
                entry[key] = ""
        description = str(entry.get("description") or "").strip()
        if not description or is_filler(description):
            entry.pop("description", None)
        else:
            entry["description"] = description
    return (json.dumps(data, ensure_ascii=False, indent=2) + "\n").encode("utf-8")


def text_bytes(path: Path) -> bytes:
    """LF siempre: en Windows git guarda LF aunque el checkout tenga CRLF."""
    return path.read_bytes().replace(b"\r\n", b"\n")


def collect(source: Path, include_images: bool) -> list[tuple[str, bytes]]:
    creatures = source / "catalog" / "creatures.json"
    if creatures.is_file():
        raw_creatures = text_bytes(creatures)
    else:
        raise SystemExit(f"No está el catálogo en {source / 'catalog'}")

    # El clima es un único ciclo anual (clima.json). Se mantiene el patrón
    # antiguo por año para poder regenerar paquetes ya publicados.
    cycle = source / "calendar" / "clima.json"
    calendar = [cycle] if cycle.is_file() else sorted((source / "calendar").glob("clima-*.json"))
    if not calendar:
        raise SystemExit(f"No hay clima en {source / 'calendar'}")

    files: list[tuple[str, bytes]] = [("catalog/creatures.json", creatures_payload(raw_creatures))]
    files.extend((f"calendar/{item.name}", text_bytes(item)) for item in calendar)
    codes = source / "codes" / "codigos.json"
    files.append(("codes/codigos.json", text_bytes(codes) if codes.is_file() else b"[]\n"))
    # El resto del paquete también debe viajar en el manifiesto.
    for relative in (
        "calendar/eventos.json",
        "catalog/CREDITOS.md",
        "huerto/siembras.json",
        "huerto/flores.json",
        "cocina/recetas.json",
    ):
        extra = source / relative
        if extra.is_file():
            files.append((relative, text_bytes(extra)))
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
