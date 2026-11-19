#!/usr/bin/env bash
# Instalación idempotente: se ejecuta al crear/reconstruir y al iniciar el Codespace.
set -euo pipefail

SDK="${ANDROID_HOME:-/home/vscode/android-sdk}"
CMDLINE="$SDK/cmdline-tools/latest/bin/sdkmanager"

# 1) Android command-line tools (sdkmanager). Solo si no están ya instalados.
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

# 2) Licencias + paquetes que necesita este proyecto (compileSdk 37, build-tools 36/37).
yes | "$CMDLINE" --sdk_root="$SDK" --licenses >/dev/null 2>&1 || true
"$CMDLINE" --sdk_root="$SDK" --install \
  "platform-tools" \
  "platforms;android-37.0" \
  "build-tools;36.0.0" \
  "build-tools;37.0.0"

# 3) OpenCode CLI (se instala en ~/.opencode/bin; se reinstala solo tras un rebuild).
if [ ! -x "$HOME/.opencode/bin/opencode" ]; then
  echo ">> Instalando OpenCode CLI"
  curl -fsSL https://opencode.ai/v2/install | bash
fi

# 4) El wrapper de Gradle debe ser ejecutable.
chmod +x ./gradlew

echo ">> Entorno listo: sdkmanager=$(command -v "$CMDLINE") opencode=$(command -v opencode)"
