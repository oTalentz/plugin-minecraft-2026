#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! compgen -G "$SCRIPT_DIR/server/plugins/Coins-"*.jar >/dev/null || ! compgen -G "$SCRIPT_DIR/server/plugins/Tags-"*.jar >/dev/null; then
    echo "Plugins nao encontrados. Executando build..."
    "$SCRIPT_DIR/build.sh"
fi

cd "$SCRIPT_DIR/server"
docker compose up -d
