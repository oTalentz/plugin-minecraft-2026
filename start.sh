#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! -f "$SCRIPT_DIR/server/plugins/Coins-"*.jar || ! -f "$SCRIPT_DIR/server/plugins/Tags-"*.jar ]]; then
    echo "Plugins nao encontrados. Executando build..."
    "$SCRIPT_DIR/build.sh"
fi

echo "Baixando plugins externos..."
"$SCRIPT_DIR/server/download-plugins.sh"

cd "$SCRIPT_DIR/server"
docker compose up -d

# Inicia tunel publico TCP (fallback do playit, que pode falhar em redes sem UDP para o servidor de controle)
"$SCRIPT_DIR/server/expose-bore.sh"
