#!/bin/bash
# Corrige la regla usblp SOLO en el disco real (el /etc en vivo es read-only).
set -euo pipefail
overlayroot-chroot sh -c '
  RULE=/etc/udev/rules.d/99-pos-liberar-usblp.rules
  cp "$RULE" "$RULE.bak-antes-bind" 2>/dev/null || true
  sed -i "s/^ACTION==\"add\",/ACTION==\"bind\",/" "$RULE"
  echo "--- Regla en el disco real ahora:"
  grep "^ACTION" "$RULE"
'
