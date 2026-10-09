#!/data/data/com.termux/files/usr/bin/bash
# TITAN Pro — helper for Termux on Android
# Usage: bash run_titan_termux.sh /path/to/TITAN_PRO_V75_PWA.py

set -e
SCRIPT="${1:-TITAN_PRO_V75_PWA.py}"

if [ ! -f "$SCRIPT" ]; then
  echo "File not found: $SCRIPT"
  echo "Copy TITAN_PRO_V75_PWA.py into Termux home first."
  exit 1
fi

echo "[TITAN] Installing deps if needed…"
pip install -q flask requests numpy pandas urllib3 websocket-client 2>/dev/null || true

export TITAN_OPEN_BROWSER=0
echo "[TITAN] Starting engine on http://127.0.0.1:5000"
echo "[TITAN] Open the TITAN Pro Android app now."
python "$SCRIPT"
