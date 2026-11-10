#!/usr/bin/env python3
"""Falla si un APK todavía lleva imágenes de catálogo o arte empaquetado como WebP/PNG de fichas."""

from __future__ import annotations

import sys
import zipfile
from pathlib import Path

BANNED_PREFIXES = (
    "assets/catalog/images/",
    "assets/content/catalog/images/",
)
BANNED_SUFFIXES = (".webp", ".png", ".jpg", ".jpeg", ".gif")


def main() -> None:
    apk = Path(sys.argv[1] if len(sys.argv) > 1 else "app/build/outputs/apk/debug")
    if apk.is_dir():
        found = sorted(apk.glob("*.apk"))
        if not found:
            raise SystemExit(f"No hay APK en {apk}")
        apk = found[-1]
    if not apk.is_file():
        raise SystemExit(f"No está el APK: {apk}")

    bad: list[str] = []
    with zipfile.ZipFile(apk) as package:
        for name in package.namelist():
            lowered = name.lower()
            if lowered.startswith(BANNED_PREFIXES):
                bad.append(name)
                continue
            if "/catalog/images/" in lowered and lowered.endswith(BANNED_SUFFIXES):
                bad.append(name)
    if bad:
        print("El APK todavía incluye arte de catálogo:")
        for name in bad:
            print(name)
        raise SystemExit(1)
    print(f"Sin imágenes de catálogo: {apk} ({apk.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
