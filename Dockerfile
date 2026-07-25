# ─── Dockerfile: Servidor Minecraft Paper 1.21.8 Multi-Versão ───
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="oTalentz"
LABEL description="Servidor Minecraft Paper 1.21.8 com suporte multi-versão"

# ─── Variáveis ───
ENV PAPER_VERSION=1.21.8
ENV VIAVERSION_VER=5.11.0
ENV VIABACKWARDS_VER=5.11.0
ENV VIAREWIND_VER=4.1.3
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
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -L -o paper-${PAPER_VERSION}.jar \
    "https://api.papermc.io/v2/projects/paper/versions/${PAPER_VERSION}/builds/latest/downloads/paper-${PAPER_VERSION}.jar" && \
    apt-get purge -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# ─── Baixar Plugins ───
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -L -o plugins/ViaVersion.jar \
    "https://github.com/ViaVersion/ViaVersion/releases/download/${VIAVERSION_VER}/ViaVersion-${VIAVERSION_VER}.jar" && \
    curl -L -o plugins/ViaBackwards.jar \
    "https://github.com/ViaVersion/ViaBackwards/releases/download/${VIABACKWARDS_VER}/ViaBackwards-${VIABACKWARDS_VER}.jar" && \
    curl -L -o plugins/ViaRewind.jar \
    "https://github.com/ViaVersion/ViaRewind/releases/download/${VIAREWIND_VER}/ViaRewind-${VIAREWIND_VER}.jar" && \
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
