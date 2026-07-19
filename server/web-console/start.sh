#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

python3 -c "import flask" 2>/dev/null || pip install --user flask

tmux kill-session -t webconsole 2>/dev/null || true
tmux new-session -d -s webconsole "python3 app.py > /tmp/webconsole.log 2>&1"

echo "Console web iniciado em segundo plano."
echo "URL local: http://localhost:5000"
echo "Senha: $(cat ../data/.console-password.txt 2>/dev/null || echo 'em /tmp/webconsole.log')"
