# ─── Dockerfile: Servidor Minecraft Paper 1.21.8 Multi-Versão ───
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="oTalentz"
LABEL description="Servidor Minecraft Paper 1.21.8 com suporte multi-versão"

# ─── Variáveis ───
ENV PAPER_VERSION=1.21.8
ENV MIN_RAM=2G
ENV MAX_RAM=4G
ENV SERVER_DIR=/server

# ─── Setup ───
RUN mkdir -p ${SERVER_DIR}/plugins/ViaVersion \
             ${SERVER_DIR}/plugins/ViaBackwards \
             ${SERVER_DIR}/plugins/ViaRewind \
             ${SERVER_DIR}/logs

WORKDIR ${SERVER_DIR}

# ─── Baixar Paper ───
# A API v2 (api.papermc.io/v2) foi DESCONTINUADA (sunset) e retorna 404.
# Usamos a API fill v3 para obter os metadados do build mais recente e
# extrair a URL real de download (hospedada em fill-data.papermc.io).
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates unzip && \
    PAPER_META=$(curl -sL "https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds/latest") && \
    PAPER_URL=$(echo "$PAPER_META" | grep -oE 'https://fill-data[^"]+\.jar' | head -1) && \
    if [ -z "$PAPER_URL" ]; then echo "ERRO: nao foi possivel obter a URL do Paper ${PAPER_VERSION}" >&2; exit 1; fi && \
    echo "Baixando Paper de: $PAPER_URL" && \
    curl -L -o paper-${PAPER_VERSION}.jar "$PAPER_URL" && \
    unzip -tqq paper-${PAPER_VERSION}.jar > /dev/null && \
    apt-get purge -y curl unzip && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# ─── Baixar Plugins ───
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -L -o plugins/ViaVersion.jar \
    "https://github.com/ViaVersion/ViaVersion/releases/download/5.3.0/ViaVersion-5.3.0.jar" && \
    curl -L -o plugins/ViaBackwards.jar \
    "https://github.com/ViaVersion/ViaBackwards/releases/download/5.3.0/ViaBackwards-5.3.0.jar" && \
    curl -L -o plugins/ViaRewind.jar \
    "https://github.com/ViaVersion/ViaRewind/releases/download/4.0.4/ViaRewind-4.0.4.jar" && \
    apt-get purge -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# ─── Copiar configs ───
COPY server.properties eula.txt ./
COPY plugins/ViaVersion/config.yml plugins/ViaVersion/config.yml
COPY plugins/ViaBackwards/config.yml plugins/ViaBackwards/config.yml

# ─── Volume para persistência ───
VOLUME ["/server/world", "/server/world_nether", "/server/world_the_end", "/server/logs", "/server/plugins"]

# ─── Porta ───
EXPOSE 25565/tcp
EXPOSE 25565/udp

# ─── Healthcheck ───
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD bash -c 'echo > /dev/tcp/localhost/25565' || exit 1

# ─── Entrypoint ───
ENTRYPOINT ["sh", "-c", "java -Xms${MIN_RAM} -Xmx${MAX_RAM} \
    -XX:+UseG1GC \
    -XX:+ParallelRefProcEnabled \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+DisableExplicitGC \
    -XX:+AlwaysPreTouch \
    -XX:G1NewSizePercent=30 \
    -XX:G1MaxNewSizePercent=40 \
    -XX:G1HeapRegionSize=8M \
    -XX:G1ReservePercent=20 \
    -XX:G1HeapWastePercent=5 \
    -XX:G1MixedGCCountTarget=4 \
    -XX:InitiatingHeapOccupancyPercent=15 \
    -XX:G1MixedGCLiveThresholdPercent=90 \
    -XX:G1RSetUpdatingPauseTimePercent=5 \
    -XX:SurvivorRatio=32 \
    -XX:+PerfDisableSharedMem \
    -XX:MaxTenuringThreshold=1 \
    -Dusing.aikars.flags=https://mcflags.emc.gs \
    -Daikars.new.flags=true \
    -jar paper-${PAPER_VERSION}.jar --nogui"]
