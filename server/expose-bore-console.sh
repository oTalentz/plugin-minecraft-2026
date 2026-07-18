#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$SCRIPT_DIR/tools"
BORE_BIN="$TOOLS_DIR/bore"
TMUX_SESSION="bore-console"
LOCAL_PORT=5000
REMOTE_PORT=55002
ID="otalentz-mc-console"

mkdir -p "$TOOLS_DIR"

if [[ ! -x "$BORE_BIN" ]]; then
    echo "Baixando bore (tunel TCP/HTTP publico)..."
    curl -fsSL -L -o "$BORE_BIN" "https://github.com/jkuri/bore/releases/download/v0.5.0/bore_linux_amd64"
    chmod +x "$BORE_BIN"
fi

if tmux has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "Tunel bore do console ja esta rodando na sessao tmux $TMUX_SESSION"
    echo "Endereco publico: http://bore.digital:$REMOTE_PORT (https://$ID.bore.digital)"
    exit 0
fi

echo "Iniciando tunel bore do console (bore.digital:$REMOTE_PORT -> localhost:$LOCAL_PORT)..."
tmux new-session -d -s "$TMUX_SESSION" "$BORE_BIN -s bore.digital -p 2200 -ls localhost -lp $LOCAL_PORT -bp $REMOTE_PORT -id $ID -a -r"

echo "Tunel publico do console ativo: http://bore.digital:$REMOTE_PORT"
