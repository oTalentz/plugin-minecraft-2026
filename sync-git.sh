#!/usr/bin/env bash
# Script de sincronização automática com Git
# Monitora mudanças no repositório e atualiza automaticamente

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCK_FILE="$SCRIPT_DIR/.sync.lock"
LAST_COMMIT_FILE="$SCRIPT_DIR/.last_commit"
INTERVAL=${SYNC_INTERVAL:-30} # Intervalo padrão: 30 segundos

# Função para verificar se outro processo está rodando
check_lock() {
    if [ -f "$LOCK_FILE" ]; then
        PID=$(cat "$LOCK_FILE")
        if kill -0 "$PID" 2>/dev/null; then
            echo "⚠️  Outro processo de sync já está rodando (PID: $PID)"
            exit 1
        else
            echo "🧹 Limpando lock file órfão..."
            rm -f "$LOCK_FILE"
        fi
    fi
}

# Função para criar lock
create_lock() {
    echo $$ > "$LOCK_FILE"
    trap 'rm -f "$LOCK_FILE"' EXIT INT TERM
}

# Função para obter o commit atual
get_current_commit() {
    git -C "$SCRIPT_DIR" rev-parse HEAD 2>/dev/null || echo "none"
}

# Função para sincronizar
do_sync() {
    local current_commit=$(get_current_commit)
    local last_commit="none"
    
    if [ -f "$LAST_COMMIT_FILE" ]; then
        last_commit=$(cat "$LAST_COMMIT_FILE")
    fi
    
    if [ "$current_commit" != "$last_commit" ]; then
        echo "🔄 Nova mudança detectada!"
        echo "   Anterior: ${last_commit:0:8}"
        echo "   Atual:    ${current_commit:0:8}"
        
        echo "📥 Executando git pull..."
        cd "$SCRIPT_DIR"
        git fetch origin
        
        # Verifica se há mudanças para merge
        LOCAL=$(git rev-parse @)
        REMOTE=$(git rev-parse @{u})
        BASE=$(git merge-base @ @{u})
        
        if [ $LOCAL = $REMOTE ]; then
            echo "✅ Já está atualizado!"
        elif [ $LOCAL = $BASE ]; then
            echo "⬇️  Fazendo pull das mudanças..."
            git pull --rebase --autostash
            
            # Salva o novo commit
            get_current_commit > "$LAST_COMMIT_FILE"
            
            echo "🔨 Executando build automático..."
            if [ -x "$SCRIPT_DIR/build.sh" ]; then
                "$SCRIPT_DIR/build.sh" || echo "⚠️  Build falhou, mas continando..."
            fi
            
            echo "✨ Sincronização completa!"
        elif [ $REMOTE = $BASE ]; then
            echo "⚠️  Você tem commits locais não publicados!"
            echo "   Por favor, faça push das suas mudanças primeiro."
        else
            echo "⚠️  Divergência detectada! Resolvendo..."
            git pull --rebase --autostash
            get_current_commit > "$LAST_COMMIT_FILE"
        fi
    fi
}

# Função principal de monitoramento
monitor() {
    echo "👁️  Iniciando monitoramento Git em tempo real..."
    echo "   Diretório: $SCRIPT_DIR"
    echo "   Intervalo: ${INTERVAL}s"
    echo "   Pressione Ctrl+C para parar"
    echo ""
    
    # Salva o commit inicial
    get_current_commit > "$LAST_COMMIT_FILE"
    
    while true; do
        do_sync
        sleep "$INTERVAL"
    done
}

# Função para sync único
sync_once() {
    echo "🔄 Executando sincronização única..."
    do_sync
}

# Parse de argumentos
case "${1:-monitor}" in
    monitor|watch)
        check_lock
        create_lock
        monitor
        ;;
    once|single)
        sync_once
        ;;
    status)
        echo "📊 Status da sincronização:"
        echo "   Último commit: $(cat "$LAST_COMMIT_FILE" 2>/dev/null || echo 'N/A')"
        echo "   Commit atual:  $(get_current_commit)"
        if [ -f "$LOCK_FILE" ]; then
            echo "   Status:        🟢 Monitorando (PID: $(cat "$LOCK_FILE"))"
        else
            echo "   Status:        🔴 Parado"
        fi
        ;;
    stop)
        if [ -f "$LOCK_FILE" ]; then
            PID=$(cat "$LOCK_FILE")
            if kill -0 "$PID" 2>/dev/null; then
                echo "🛑 Parando processo de sync (PID: $PID)..."
                kill "$PID"
                rm -f "$LOCK_FILE"
            else
                echo "🧹 Limpando lock file órfão..."
                rm -f "$LOCK_FILE"
            fi
        else
            echo "ℹ️  Nenhum processo de sync está rodando."
        fi
        ;;
    *)
        echo "Uso: $0 {monitor|once|status|stop}"
        echo ""
        echo "Comandos:"
        echo "  monitor  - Monitora continuamente (padrão)"
        echo "  once     - Executa sincronização única"
        echo "  status   - Mostra status atual"
        echo "  stop     - Para o monitoramento"
        echo ""
        echo "Variáveis de ambiente:"
        echo "  SYNC_INTERVAL - Intervalo em segundos (padrão: 30)"
        exit 1
        ;;
esac
