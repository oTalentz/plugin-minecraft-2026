#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$SCRIPT_DIR/tools"
BORE_BIN="$TOOLS_DIR/bore"
TMUX_SESSION="bore-minecraft"
LOCAL_PORT=25565
REMOTE_PORT=55001

mkdir -p "$TOOLS_DIR"

if [[ ! -x "$BORE_BIN" ]]; then
    echo "Baixando bore (tunel TCP publico)..."
    curl -fsSL -L -o "$BORE_BIN" "https://github.com/jkuri/bore/releases/download/v0.5.0/bore_linux_amd64"
    chmod +x "$BORE_BIN"
fi

if tmux has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "Tunel bore ja esta rodando na sessao tmux $TMUX_SESSION"
    echo "Endereco publico: bore.digital:$REMOTE_PORT"
    exit 0
fi

echo "Iniciando tunel bore (bore.digital:$REMOTE_PORT -> localhost:$LOCAL_PORT)..."
tmux new-session -d -s "$TMUX_SESSION" "$BORE_BIN -s bore.digital -p 2200 -ls localhost -lp $LOCAL_PORT -bp $REMOTE_PORT -a -r"

echo "Tunel publico ativo: bore.digital:$REMOTE_PORT"
