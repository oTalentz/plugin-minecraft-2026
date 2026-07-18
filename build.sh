#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
M2_HOME="$SCRIPT_DIR/tools/apache-maven-3.9.6"
MVN="$M2_HOME/bin/mvn"

if [[ ! -x "$MVN" ]]; then
    if [[ -f "$SCRIPT_DIR/maven.zip" ]]; then
        echo "Extraindo Maven..."
        mkdir -p "$SCRIPT_DIR/tools"
        unzip -q "$SCRIPT_DIR/maven.zip" -d "$SCRIPT_DIR/tools"
    else
        echo "Maven nao encontrado em $M2_HOME e maven.zip ausente"
        exit 1
    fi
fi

mkdir -p "$SCRIPT_DIR/server/plugins"

echo "=== Compilando Coins ==="
"$MVN" -f "$SCRIPT_DIR/plugins/Coins/pom.xml" clean package -q

echo "=== Compilando Tags ==="
"$MVN" -f "$SCRIPT_DIR/plugins/Tags/pom.xml" clean package -q

echo "=== Compilando Tab ==="
"$MVN" -f "$SCRIPT_DIR/plugins/Tab/pom.xml" clean package -q

cp "$SCRIPT_DIR/plugins/Coins/target/Coins-"*.jar "$SCRIPT_DIR/server/plugins/"
cp "$SCRIPT_DIR/plugins/Tags/target/Tags-"*.jar "$SCRIPT_DIR/server/plugins/"
cp "$SCRIPT_DIR/plugins/Tab/target/Tab-"*.jar "$SCRIPT_DIR/server/plugins/"

echo "=== Plugins gerados em server/plugins/ ==="
ls -la "$SCRIPT_DIR/server/plugins/"
