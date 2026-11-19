#!/usr/bin/env bash
# Instalación idempotente: se ejecuta al crear/reconstruir y al iniciar el Codespace.
# Sin `set -e`: si falla el SDK, OpenCode queda instalado igual (es lo que se usa desde el móvil).
set -uo pipefail

OPENCODE_BIN="$HOME/.opencode/bin/opencode"
export PATH="$HOME/.opencode/bin:$PATH"

# 1) OpenCode CLI PRIMERO. Es imprescindible para emparejar el celular.
if [ ! -x "$OPENCODE_BIN" ]; then
  echo ">> Instalando OpenCode CLI..."
  curl -fsSL https://opencode.ai/v2/install | bash
fi
if [ -x "$OPENCODE_BIN" ]; then
  echo ">> OpenCode listo: $($OPENCODE_BIN --version)"
else
  echo "!! No se pudo instalar OpenCode" >&2
fi

# Persistir el PATH para terminales nuevos (por si containerEnv no se aplicó).
if ! grep -qF '.opencode/bin' "$HOME/.bashrc" 2>/dev/null; then
  echo 'export PATH="$HOME/.opencode/bin:$PATH"' >> "$HOME/.bashrc"
fi

SDK="${ANDROID_HOME:-/home/vscode/android-sdk}"
CMDLINE="$SDK/cmdline-tools/latest/bin/sdkmanager"

# 2) Android command-line tools (sdkmanager). Solo si no están ya instalados.
if [ ! -x "$CMDLINE" ]; then
  echo ">> Instalando Android command-line tools en $SDK"
  if ! command -v unzip >/dev/null 2>&1; then
    sudo apt-get update -qq
    sudo apt-get install -y -qq unzip
  fi
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/cmdline-tools.zip" \
    https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q "$tmp/cmdline-tools.zip" -d "$tmp"
  mkdir -p "$SDK/cmdline-tools"
  rm -rf "$SDK/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi

# 3) Licencias + paquetes del proyecto (compileSdk 37.0, build-tools 36/37).
if [ -x "$CMDLINE" ]; then
  yes | "$CMDLINE" --sdk_root="$SDK" --licenses >/dev/null 2>&1 || true
  "$CMDLINE" --sdk_root="$SDK" --install \
    "platform-tools" \
    "platforms;android-37.0" \
    "build-tools;36.0.0" \
    "build-tools;37.0.0" || echo "!! Aviso: no se pudieron instalar todos los paquetes del SDK" >&2
else
  echo "!! Aviso: sdkmanager no disponible, no se instaló el SDK" >&2
fi

# 4) El wrapper de Gradle debe ser ejecutable.
chmod +x ./gradlew

echo ">> Entorno listo. Comprueba: opencode --version"
