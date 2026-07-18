@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "PAPER_JAR=paper-1.21.8-60.jar"
set "PAPER_URL=https://fill-data.papermc.io/v1/objects/8de7c52c3b02403503d16fac58003f1efef7dd7a0256786843927fa92ee57f1e/paper-1.21.8-60.jar"

java -version >nul 2>&1
if errorlevel 1 (
    echo Java nao foi encontrado. Instale o Java 21 para rodar Paper 1.21.8.
    pause
    exit /b 1
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VERSION=%%v"
)
echo Versao do Java detectada: %JAVA_VERSION%

echo %JAVA_VERSION% | findstr /i "21" >nul
if errorlevel 1 (
    echo AVISO: Paper 1.21.8 recomenda Java 21. Versao encontrada: %JAVA_VERSION%
)

if not exist "plugins\Coins-*.jar" (
    echo AVISO: Plugin Coins nao encontrado em plugins\. Compile o projeto antes.
)
if not exist "plugins\Tags-*.jar" (
    echo AVISO: Plugin Tags nao encontrado em plugins\. Compile o projeto antes.
)

if not exist "%PAPER_JAR%" (
    echo Baixando %PAPER_JAR% ...
    powershell -Command "Invoke-WebRequest -Uri '%PAPER_URL%' -OutFile '%PAPER_JAR%'" >nul 2>&1
    if errorlevel 1 (
        echo Falha ao baixar o Paper. Verifique a conexao.
        pause
        exit /b 1
    )
    echo Download do Paper concluido.
)

if not exist "plugins" mkdir plugins

if not exist "plugins\ViaVersion-*.jar" (
    echo Baixando ViaVersion ...
    powershell -Command "Invoke-WebRequest -Uri 'https://api.spiget.org/v2/resources/19254/download' -OutFile 'plugins\ViaVersion.jar'" >nul 2>&1
)

if not exist "plugins\ViaBackwards-*.jar" (
    echo Baixando ViaBackwards ...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/ViaVersion/ViaBackwards/releases/download/5.11.0/ViaBackwards-5.11.0.jar' -OutFile 'plugins\ViaBackwards-5.11.0.jar'" >nul 2>&1
)

if not exist "plugins\LuckPerms-*.jar" (
    echo Baixando LuckPerms ...
    powershell -Command "Invoke-WebRequest -Uri 'https://api.spiget.org/v2/resources/28140/download' -OutFile 'plugins\LuckPerms.jar'" >nul 2>&1
)

if not exist "plugins\Vault-*.jar" (
    echo Baixando Vault ...
    powershell -Command "Invoke-WebRequest -Uri 'https://api.spiget.org/v2/resources/34315/download' -OutFile 'plugins\Vault.jar'" >nul 2>&1
)

if not exist "eula.txt" (
    echo eula=true > eula.txt
    echo Arquivo eula.txt criado com eula=true.
)

:loop
java -Xms2G -Xmx2G -jar "%PAPER_JAR%" --nogui
echo Servidor encerrado. Reiniciando em 5 segundos... (Ctrl+C para cancelar)
timeout /t 5 /nobreak >nul
goto loop
