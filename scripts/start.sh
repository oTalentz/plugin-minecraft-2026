#!/usr/bin/env bash
#
# start.sh — Inicia o servidor Minecraft Paper 1.21.8
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$ROOT_DIR"

# ─── Configuração de RAM ───
MIN_RAM="${MIN_RAM:-2G}"
MAX_RAM="${MAX_RAM:-4G}"

# ─── Verificar JAR ───
PAPER_JAR="paper-1.21.8.jar"
if [ ! -f "$PAPER_JAR" ]; then
    echo "[ERROR] $PAPER_JAR não encontrado. Execute ./scripts/setup.sh primeiro."
    exit 1
fi

# ─── Flags de otimização (Aikar's Flags) ───
JAVA_FLAGS=(
    -Xms"${MIN_RAM}"
    -Xmx"${MAX_RAM}"
    -XX:+UseG1GC
    -XX:+ParallelRefProcEnabled
    -XX:MaxGCPauseMillis=200
    -XX:+UnlockExperimentalVMOptions
    -XX:+DisableExplicitGC
    -XX:+AlwaysPreTouch
    -XX:G1NewSizePercent=30
    -XX:G1MaxNewSizePercent=40
    -XX:G1HeapRegionSize=8M
    -XX:G1ReservePercent=20
    -XX:G1HeapWastePercent=5
    -XX:G1MixedGCCountTarget=4
    -XX:InitiatingHeapOccupancyPercent=15
    -XX:G1MixedGCLiveThresholdPercent=90
    -XX:G1RSetUpdatingPauseTimePercent=5
    -XX:SurvivorRatio=32
    -XX:+PerfDisableSharedMem
    -XX:MaxTenuringThreshold=1
    -Dusing.aikars.flags=https://mcflags.emc.gs
    -Daikars.new.flags=true
)

echo "========================================="
echo "  Iniciando Servidor Minecraft 1.21.8"
echo "  RAM: ${MIN_RAM} - ${MAX_RAM}"
echo "  Porta: $(grep 'server-port' server.properties | cut -d'=' -f2)"
echo "========================================="
echo ""

exec java "${JAVA_FLAGS[@]}" -jar "$PAPER_JAR" --nogui
