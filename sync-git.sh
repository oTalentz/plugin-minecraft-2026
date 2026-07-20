#!/usr/bin/env bash
set -euo pipefail

INTERVAL=${SYNC_INTERVAL:-30} # Padrão: 30 segundos
LOCKFILE="/tmp/git-sync-$$.lock"

# Limpar lockfile ao sair
cleanup() {
    rm -f "$LOCKFILE" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

check_git() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        echo "❌ Erro: Não é um repositório Git."
        exit 1
    fi
}

sync_once() {
    echo "🔄 Verificando atualizações..."

    # Detectar branch atual
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

    # Busca mudanças remotas
    if ! git fetch origin; then
        echo "❌ Erro: Falha ao fazer fetch do repositório remoto."
        return 1
    fi

    # Compara branch atual com a remota
    LOCAL=$(git rev-parse HEAD)
    REMOTE=$(git rev-parse "origin/$CURRENT_BRANCH" 2>/dev/null || echo "")

    if [ -z "$REMOTE" ]; then
        echo "❌ Erro: Branch remota 'origin/$CURRENT_BRANCH' não encontrada."
        return 1
    fi

    if [ "$LOCAL" != "$REMOTE" ]; then
        echo "📥 Novas atualizações detectadas! Baixando..."
        # Pull com rebase para manter histórico limpo
        if git pull --rebase --autostash; then
            echo "✅ Sincronizado com sucesso!"
            # Opcional: Rodar build se existir script de build
            if [ -f "build.sh" ]; then ./build.sh; fi
            if [ -f "package.json" ] && [ -d "node_modules" ]; then npm install 2>/dev/null; fi
        else
            echo "❌ Erro ao atualizar. Conflitos podem existir."
            return 1
        fi
    else
        echo "✔️ Já está atualizado."
    fi
}

case "$1" in
    monitor)
        check_git
        echo "👁️ Iniciando monitoramento (intervalo: ${INTERVAL}s). Pressione Ctrl+C para parar."
        while true; do
            sync_once
            sleep $INTERVAL
        done
        ;;
    once)
        check_git
        sync_once
        ;;
    status)
        git status
        git fetch origin
        git status
        ;;
    *)
        echo "Uso: $0 {monitor|once|status}"
        echo "  monitor : Verifica e atualiza a cada ${INTERVAL}s"
        echo "  once    : Verifica e atualiza uma vez"
        echo "  status  : Mostra status do git"
        exit 1
        ;;
esac