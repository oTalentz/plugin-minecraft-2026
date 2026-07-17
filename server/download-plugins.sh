#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/plugins"
USER_AGENT="cool-project/1.0.0"

echo "Baixando plugins externos..."

mkdir -p "$OUTPUT_DIR"
cd "$OUTPUT_DIR"

# ViaVersion (ultimo release estavel no Hangar)
VIA_VERSION_URL=$(curl -fsSL -H "User-Agent: $USER_AGENT" "https://hangar.papermc.io/api/v1/projects/ViaVersion/ViaVersion/versions?limit=1&offset=0&channel=Release" | python3 -c "import json,sys; print(json.load(sys.stdin)['result'][0]['downloads']['PAPER']['downloadUrl'])")
curl -fsSL -H "User-Agent: $USER_AGENT" "$VIA_VERSION_URL" -o ViaVersion.jar

# ViaBackwards (ultimo release estavel no Hangar)
VIA_BACKWARDS_URL=$(curl -fsSL -H "User-Agent: $USER_AGENT" "https://hangar.papermc.io/api/v1/projects/ViaVersion/ViaBackwards/versions?limit=1&offset=0&channel=Release" | python3 -c "import json,sys; print(json.load(sys.stdin)['result'][0]['downloads']['PAPER']['downloadUrl'])")
curl -fsSL -H "User-Agent: $USER_AGENT" "$VIA_BACKWARDS_URL" -o ViaBackwards.jar

# LuckPerms (ultimo Bukkit)
LUCKPERMS_URL=$(curl -fsSL "https://metadata.luckperms.net/data/downloads" | python3 -c "import json,sys; print(json.load(sys.stdin)['downloads']['bukkit'])")
curl -fsSL "$LUCKPERMS_URL" -o LuckPerms-Bukkit.jar

# Vault (release estavel 1.7.3)
curl -fsSL -L "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar" -o Vault.jar

# playit.gg (tunel publico para o servidor)
curl -fsSL -L "https://github.com/playit-cloud/playit-minecraft-plugin/releases/latest/download/playit-minecraft-plugin.jar" -o playit-minecraft-plugin.jar

echo "Plugins externos baixados:"
ls -la "$OUTPUT_DIR"/ViaVersion.jar "$OUTPUT_DIR"/ViaBackwards.jar "$OUTPUT_DIR"/LuckPerms-Bukkit.jar "$OUTPUT_DIR"/Vault.jar "$OUTPUT_DIR"/playit-minecraft-plugin.jar
