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
VIAVERSION_VER="5.3.0"
VIABACKWARDS_VER="5.3.0"
VIAREWIND_VER="4.0.4"

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
    info "Baixando Paper $PAPER_VERSION..."
    PAPER_URL="https://api.papermc.io/v2/projects/paper/versions/${PAPER_VERSION}/builds/latest/downloads/paper-${PAPER_VERSION}.jar"
    if command -v curl &> /dev/null; then
        curl -L -o "$PAPER_JAR" "$PAPER_URL"
    elif command -v wget &> /dev/null; then
        wget -O "$PAPER_JAR" "$PAPER_URL"
    else
        error "curl ou wget é necessário para download."
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
ok "  Plugins:  ViaVersion, ViaBackwards, ViaRewind"
ok "========================================="
echo ""
info "Para iniciar o servidor, execute:"
info "  ./scripts/start.sh"
echo ""
