@echo off
REM setup.bat — Baixa e configura o servidor Minecraft 1.21.8 (Paper) com multi-versão
setlocal enabledelayedexpansion

set "PAPER_VERSION=1.21.8"
set "VIAVERSION_VER=5.3.0"
set "VIABACKWARDS_VER=5.3.0"
set "VIAREWIND_VER=4.0.4"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo [INFO] Verificando Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java nao encontrado! Instale o Java 21 JRE/JDK.
    pause
    exit /b 1
)
echo [OK] Java detectado.

echo [INFO] Criando estrutura de diretorios...
if not exist "plugins\ViaVersion" mkdir "plugins\ViaVersion"
if not exist "plugins\ViaBackwards" mkdir "plugins\ViaBackwards"
if not exist "plugins\ViaRewind" mkdir "plugins\ViaRewind"
if not exist "logs" mkdir "logs"
echo [OK] Diretorios criados.

REM --- Baixar Paper ---
set "PAPER_JAR=paper-%PAPER_VERSION%.jar"
if exist "%PAPER_JAR%" (
    echo [WARN] %PAPER_JAR% ja existe. Pulando download.
) else (
    echo [INFO] Baixando Paper %PAPER_VERSION%...
    REM A API v2 foi DESCONTINUADA (sunset). Usamos a API fill v3 para obter a URL real.
    powershell -Command "$meta = Invoke-RestMethod -Uri 'https://fill.papermc.io/v3/projects/paper/versions/%PAPER_VERSION%/builds/latest'; Invoke-WebRequest -Uri $meta.downloads.'server:default'.url -OutFile '%PAPER_JAR%'"
    echo [OK] Paper baixado.
)

REM --- Baixar ViaVersion ---
set "VIA_JAR=plugins\ViaVersion-%VIAVERSION_VER%.jar"
if exist "%VIA_JAR%" (
    echo [WARN] ViaVersion ja existe. Pulando download.
) else (
    echo [INFO] Baixando ViaVersion %VIAVERSION_VER%...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/ViaVersion/ViaVersion/releases/download/%VIAVERSION_VER%/ViaVersion-%VIAVERSION_VER%.jar' -OutFile '%VIA_JAR%'"
    echo [OK] ViaVersion baixado.
)

REM --- Baixar ViaBackwards ---
set "VB_JAR=plugins\ViaBackwards-%VIABACKWARDS_VER%.jar"
if exist "%VB_JAR%" (
    echo [WARN] ViaBackwards ja existe. Pulando download.
) else (
    echo [INFO] Baixando ViaBackwards %VIABACKWARDS_VER%...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/ViaVersion/ViaBackwards/releases/download/%VIABACKWARDS_VER%/ViaBackwards-%VIABACKWARDS_VER%.jar' -OutFile '%VB_JAR%'"
    echo [OK] ViaBackwards baixado.
)

REM --- Baixar ViaRewind ---
set "VR_JAR=plugins\ViaRewind-%VIAREWIND_VER%.jar"
if exist "%VR_JAR%" (
    echo [WARN] ViaRewind ja existe. Pulando download.
) else (
    echo [INFO] Baixando ViaRewind %VIAREWIND_VER%...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/ViaVersion/ViaRewind/releases/download/%VIAREWIND_VER%/ViaRewind-%VIAREWIND_VER%.jar' -OutFile '%VR_JAR%'"
    echo [OK] ViaRewind baixado.
)

echo.
echo =========================================
echo   Setup concluido com sucesso!
echo   Servidor: Paper %PAPER_VERSION%
echo   Plugins:  ViaVersion, ViaBackwards, ViaRewind
echo =========================================
echo.
echo [INFO] Para iniciar o servidor, execute:
echo   scripts\start.bat
echo.
pause
