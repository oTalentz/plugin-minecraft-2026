#!/usr/bin/env bash
#
# setup.sh — Baixa e configura o servidor Minecraft 1.21.8 (Paper) com multi-versão
#
set -euo pipefail

# ─── Cores ───
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${CYAN}[INFO]${NC} $*"; }
ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ─── Versões ───
PAPER_VERSION="1.21.8"
VIAVERSION_VER="5.11.0"
VIABACKWARDS_VER="5.11.0"
VIAREWIND_VER="4.1.3"

# ─── Diretórios ───
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
PLUGINS_DIR="$ROOT_DIR/plugins"

cd "$ROOT_DIR"

# ─── Verificar Java ───
info "Verificando Java..."
if ! command -v java &> /dev/null; then
    error "Java não encontrado! Instale o Java 21 JRE/JDK."
fi

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 21 ]; then
    error "Java 21+ é necessário. Versão encontrada: $JAVA_VER"
fi
ok "Java $JAVA_VER detectado."

# ─── Criar estrutura ───
info "Criando estrutura de diretórios..."
mkdir -p "$PLUGINS_DIR/ViaVersion"
mkdir -p "$PLUGINS_DIR/ViaBackwards"
mkdir -p "$PLUGINS_DIR/ViaRewind"
mkdir -p "$ROOT_DIR/logs"
ok "Diretórios criados."

# ─── Baixar Paper ───
PAPER_JAR="paper-${PAPER_VERSION}.jar"
if [ -f "$PAPER_JAR" ]; then
    warn "$PAPER_JAR já existe. Pulando download."
else
    # A API v2 (api.papermc.io/v2) foi DESCONTINUADA (sunset).
    # Usamos a nova API fill v3 para obter os metadados do build mais recente
    # e extrair a URL real de download (hospedada em fill-data.papermc.io).
    info "Obtendo metadados do build mais recente do Paper $PAPER_VERSION..."
    PAPER_META_URL="https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds/latest"
    if command -v curl &> /dev/null; then
        PAPER_META=$(curl -sL "$PAPER_META_URL")
    elif command -v wget &> /dev/null; then
        PAPER_META=$(wget -qO- "$PAPER_META_URL")
    else
        error "curl ou wget é necessário para download."
    fi
    PAPER_URL=$(echo "$PAPER_META" | grep -oE 'https://fill-data[^"]+\.jar' | head -1)
    if [ -z "$PAPER_URL" ]; then
        error "Não foi possível obter a URL de download do Paper. Verifique a conexão ou se a versão ${PAPER_VERSION} existe."
    fi
    info "Baixando Paper $PAPER_VERSION..."
    info "URL: $PAPER_URL"
    if command -v curl &> /dev/null; then
        curl -L -o "$PAPER_JAR" "$PAPER_URL"
    else
        wget -O "$PAPER_JAR" "$PAPER_URL"
    fi
    ok "Paper $PAPER_VERSION baixado."
fi

# ─── Baixar ViaVersion ───
VIA_JAR="$PLUGINS_DIR/ViaVersion-${VIAVERSION_VER}.jar"
if [ -f "$VIA_JAR" ]; then
    warn "ViaVersion já existe. Pulando download."
else
    info "Baixando ViaVersion $VIAVERSION_VER..."
    VIA_URL="https://github.com/ViaVersion/ViaVersion/releases/download/${VIAVERSION_VER}/ViaVersion-${VIAVERSION_VER}.jar"
    if command -v curl &> /dev/null; then
        curl -L -o "$VIA_JAR" "$VIA_URL"
    else
        wget -O "$VIA_JAR" "$VIA_URL"
    fi
    ok "ViaVersion baixado."
fi

# ─── Baixar ViaBackwards ───
VB_JAR="$PLUGINS_DIR/ViaBackwards-${VIABACKWARDS_VER}.jar"
if [ -f "$VB_JAR" ]; then
    warn "ViaBackwards já existe. Pulando download."
else
    info "Baixando ViaBackwards $VIABACKWARDS_VER..."
    VB_URL="https://github.com/ViaVersion/ViaBackwards/releases/download/${VIABACKWARDS_VER}/ViaBackwards-${VIABACKWARDS_VER}.jar"
    if command -v curl &> /dev/null; then
        curl -L -o "$VB_JAR" "$VB_URL"
    else
        wget -O "$VB_JAR" "$VB_URL"
    fi
    ok "ViaBackwards baixado."
fi

# ─── Baixar ViaRewind ───
VR_JAR="$PLUGINS_DIR/ViaRewind-${VIAREWIND_VER}.jar"
if [ -f "$VR_JAR" ]; then
    warn "ViaRewind já existe. Pulando download."
else
    info "Baixando ViaRewind $VIAREWIND_VER..."
    VR_URL="https://github.com/ViaVersion/ViaRewind/releases/download/${VIAREWIND_VER}/ViaRewind-${VIAREWIND_VER}.jar"
    if command -v curl &> /dev/null; then
        curl -L -o "$VR_JAR" "$VR_URL"
    else
        wget -O "$VR_JAR" "$VR_URL"
    fi
    ok "ViaRewind baixado."
fi

# ─── Copiar configs dos plugins ───
info "Copiando configurações dos plugins..."
if [ -f "$ROOT_DIR/plugins/ViaVersion/config.yml" ]; then
    ok "Config do ViaVersion já presente."
fi
if [ -f "$ROOT_DIR/plugins/ViaBackwards/config.yml" ]; then
    ok "Config do ViaBackwards já presente."
fi

echo ""
ok "========================================="
ok "  Setup concluído com sucesso!"
ok "  Servidor: Paper $PAPER_VERSION"
ok "  Plugins:  ViaVersion $VIAVERSION_VER, ViaBackwards $VIABACKWARDS_VER, ViaRewind $VIAREWIND_VER"
ok "========================================="
echo ""
info "Para iniciar o servidor, execute:"
info "  ./scripts/start.sh"
echo ""
