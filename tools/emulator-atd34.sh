#!/usr/bin/env bash
# Emulador headless sin KVM (como en 1.0.4): aosp_atd API 34 + SwiftShader.
set -euo pipefail
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/platform-tools:$PATH"

if ! avdmanager list avd 2>/dev/null | grep -q heartopia-atd34; then
  yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" "emulator" "system-images;android-34;aosp_atd;x86_64"
  echo "no" | avdmanager create avd -n heartopia-atd34 -k "system-images;android-34;aosp_atd;x86_64" -d pixel_6
fi

SESSION="${EMULATOR_TMUX_SESSION:-emulator-atd34}"
tmux -f /exec-daemon/tmux.portal.conf kill-session -t "=$SESSION" 2>/dev/null || true
tmux -f /exec-daemon/tmux.portal.conf new-session -d -s "$SESSION" -c "${1:-$PWD}" -- "${SHELL:-bash}" -l
tmux -f /exec-daemon/tmux.portal.conf send-keys -t "$SESSION:0.0" \
  "emulator -avd heartopia-atd34 -no-window -gpu swiftshader_indirect -no-snapshot -memory 2048 -no-audio -no-boot-anim -no-accel" C-m
echo "Esperando arranque (puede tardar varios minutos con -no-accel)…"
for _ in $(seq 1 180); do
  if adb devices 2>/dev/null | grep -qE 'emulator-[0-9]+[[:space:]]+device'; then
    boot=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    if [ "$boot" = "1" ]; then
      adb devices
      exit 0
    fi
  fi
  sleep 5
done
echo "El emulador no terminó de arrancar a tiempo." >&2
exit 1