@echo off
REM start.bat — Inicia o servidor Minecraft Paper 1.21.8
setlocal

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

set "MIN_RAM=2G"
set "MAX_RAM=4G"
set "PAPER_JAR=paper-1.21.8.jar"

if not exist "%PAPER_JAR%" (
    echo [ERROR] %PAPER_JAR% nao encontrado. Execute scripts\setup.bat primeiro.
    pause
    exit /b 1
)

echo =========================================
echo   Iniciando Servidor Minecraft 1.21.8
echo   RAM: %MIN_RAM% - %MAX_RAM%
echo =========================================
echo.

java -Xms%MIN_RAM% -Xmx%MAX_RAM% ^
    -XX:+UseG1GC ^
    -XX:+ParallelRefProcEnabled ^
    -XX:MaxGCPauseMillis=200 ^
    -XX:+UnlockExperimentalVMOptions ^
    -XX:+DisableExplicitGC ^
    -XX:+AlwaysPreTouch ^
    -XX:G1NewSizePercent=30 ^
    -XX:G1MaxNewSizePercent=40 ^
    -XX:G1HeapRegionSize=8M ^
    -XX:G1ReservePercent=20 ^
    -XX:G1HeapWastePercent=5 ^
    -XX:G1MixedGCCountTarget=4 ^
    -XX:InitiatingHeapOccupancyPercent=15 ^
    -XX:G1MixedGCLiveThresholdPercent=90 ^
    -XX:G1RSetUpdatingPauseTimePercent=5 ^
    -XX:SurvivorRatio=32 ^
    -XX:+PerfDisableSharedMem ^
    -XX:MaxTenuringThreshold=1 ^
    -Dusing.aikars.flags=https://mcflags.emc.gs ^
    -Daikars.new.flags=true ^
    -jar "%PAPER_JAR%" --nogui

pause
