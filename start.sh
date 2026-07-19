#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! ls "$SCRIPT_DIR/server/data/plugins"/Coins-*.jar >/dev/null 2>&1 || \
   ! ls "$SCRIPT_DIR/server/data/plugins"/Tags-*.jar >/dev/null 2>&1 || \
   ! ls "$SCRIPT_DIR/server/data/plugins"/Tab-*.jar >/dev/null 2>&1; then
    echo "Plugins nao encontrados. Executando build..."
    "$SCRIPT_DIR/build.sh"
fi

echo "Baixando plugins externos..."
"$SCRIPT_DIR/server/download-plugins.sh"

cd "$SCRIPT_DIR/server"
docker compose up -d

# Inicia tunel publico TCP (fallback do playit, que pode falhar em redes sem UDP para o servidor de controle)
"$SCRIPT_DIR/server/expose-bore.sh"

# Inicia console web local para gerenciar o servidor
"$SCRIPT_DIR/server/web-console/start.sh"

# Inicia tunel publico HTTP para o console/resource pack
"$SCRIPT_DIR/server/expose-bore-console.sh"
