@echo off
REM sync-git.bat — Sincroniza o repositorio com o Git remoto
REM Uso: scripts\sync-git.bat [once^|monitor^|status^|push^|full]
setlocal enabledelayedexpansion

REM --- Cores ---
for /f %%A in ('echo prompt $E ^| cmd') do set "ESC=%%A"
set "RED=%ESC%[91m"
set "GREEN=%ESC%[92m"
set "YELLOW=%ESC%[93m"
set "CYAN=%ESC%[96m"
set "NC=%ESC%[0m"

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

REM --- Verificar se e um repositorio Git ---
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Nao e um repositorio Git.
    pause
    exit /b 1
)

REM --- Detectar branch atual ---
for /f "delims=" %%B in ('git rev-parse --abbrev-ref HEAD 2^>nul') do set "BRANCH=%%B"
if "%BRANCH%"=="" (
    echo %RED%[ERROR]%NC% Nao foi possivel detectar a branch atual.
    pause
    exit /b 1
)
if "%BRANCH%"=="HEAD" (
    echo %RED%[ERROR]%NC% Voce esta em estado detached HEAD. Faca checkout de uma branch primeiro.
    pause
    exit /b 1
)

REM --- Verificar se existe upstream ---
git rev-parse --abbrev-ref --symbolic-full-name "@{u}" >nul 2>&1
if errorlevel 1 (
    echo %YELLOW%[WARN]%NC% Branch "%BRANCH%" nao tem upstream configurado.
    echo %CYAN%[INFO]%NC% Configurando upstream para origin/%BRANCH%...
    git branch --set-upstream-to="origin/%BRANCH%" "%BRANCH%" >nul 2>&1
    if errorlevel 1 (
        echo %CYAN%[INFO]%NC% Branch remota nao encontrada. Tentando push para criar...
        git push -u origin "%BRANCH%"
        if errorlevel 1 (
            echo %RED%[ERROR]%NC% Nao foi possivel configurar upstream.
            pause
            exit /b 1
        )
    )
)

REM --- Intervalo de monitoramento (padrao: 5s) ---
if "%SYNC_INTERVAL%"=="" set "SYNC_INTERVAL=5"

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=monitor"

if /i "%ACTION%"=="monitor" goto :monitor
if /i "%ACTION%"=="once" goto :once
if /i "%ACTION%"=="status" goto :status
if /i "%ACTION%"=="push" goto :push
if /i "%ACTION%"=="full" goto :full

echo %CYAN%[INFO]%NC% Branch atual: %BRANCH%
echo.
echo Uso: %~nx0 [once^|monitor^|status^|push^|full]
echo   once    : Baixa atualizacoes remotas (pull)
echo   monitor : Verifica e atualiza a cada %SYNC_INTERVAL%s
echo   status  : Mostra status do git
echo   push    : Envia mudancas locais (add + commit + push)
echo   full    : Baixa e envia (pull + push)
echo.
echo Variavel de ambiente: SYNC_INTERVAL (padrao: 5)
pause
exit /b 1

REM ============================================================
REM  MONITOR — Loop continuo
REM ============================================================
:monitor
echo %CYAN%[INFO]%NC% Branch: %BRANCH% ^| Intervalo: %SYNC_INTERVAL%s ^| Modo: bidirecional
echo %CYAN%[INFO]%NC% Pressione Ctrl+C para parar.
echo.
:monitor_loop
call :sync_pull
if errorlevel 1 (
    echo %YELLOW%[WARN]%NC% Falha no pull. Tentando novamente em %SYNC_INTERVAL%s...
) else (
    call :push_internal
    if errorlevel 1 (
        echo %YELLOW%[WARN]%NC% Falha no push. Tentando novamente em %SYNC_INTERVAL%s...
    )
)
echo %CYAN%[INFO]%NC% Proxima verificacao em %SYNC_INTERVAL%s...
timeout /t %SYNC_INTERVAL% /nobreak >nul
if errorlevel 1 goto :monitor_end
goto :monitor_loop
:monitor_end
echo.
echo %YELLOW%[WARN]%NC% Monitoramento interrompido.
pause
exit /b 0

REM ============================================================
REM  ONCE — Pull unico
REM ============================================================
:once
call :sync_pull
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha na sincronizacao.
    pause
    exit /b 1
)
pause
exit /b 0

REM ============================================================
REM  STATUS — Mostra status
REM ============================================================
:status
echo %CYAN%[INFO]%NC% Branch atual: %BRANCH%
echo.
echo %CYAN%=== Status local ===%NC%
git status
echo.
echo %CYAN%=== Buscando atualizacoes remotas... ===%NC%
git fetch origin 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha no fetch.
    pause
    exit /b 1
)
echo.
echo %CYAN%=== Status apos fetch ===%NC%
git status
echo.
echo %CYAN%=== Commits locais nao enviados ===%NC%
git log origin/%BRANCH%..HEAD --oneline 2>nul
echo.
echo %CYAN%=== Commits remotos nao baixados ===%NC%
git log HEAD..origin/%BRANCH% --oneline 2>nul
pause
exit /b 0

REM ============================================================
REM  PUSH — Envia mudancas locais
REM ============================================================
:push
echo %CYAN%[INFO]%NC% Branch: %BRANCH%
echo.

REM --- Verificar se ha mudancas para commit ---
git diff --quiet --exit-code >nul 2>&1
set "HAS_UNSTAGED=%errorlevel%"
git diff --cached --quiet --exit-code >nul 2>&1
set "HAS_STAGED=%errorlevel%"
git ls-files --others --exclude-standard 2>nul | findstr /r "." >nul 2>&1
set "HAS_UNTRACKED=%errorlevel%"

if "%HAS_UNSTAGED%"=="0" if "%HAS_STAGED%"=="0" if "%HAS_UNTRACKED%"=="1" (
    echo %GREEN%[OK]%NC% Nenhuma mudanca local para enviar.
    echo.
    echo %CYAN%[INFO]%NC% Verificando commits nao enviados...
    git log origin/%BRANCH%..HEAD --oneline 2>nul
    git push origin "%BRANCH%"
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Falha ao enviar.
        pause
        exit /b 1
    )
    echo %GREEN%[OK]%NC% Push concluido!
    pause
    exit /b 0
)

echo %YELLOW%[WARN]%NC% Existem mudancas locais nao commitadas.
echo.
git status --short
echo.
set /p "MSG=Digite a mensagem de commit (ou Enter para pular commit): "
if "%MSG%"=="" (
    echo %CYAN%[INFO]%NC% Pulando commit. Apenas enviando commits existentes...
) else (
    echo %CYAN%[INFO]%NC% Adicionando arquivos...
    git add -A
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Falha ao adicionar arquivos.
        pause
        exit /b 1
    )
    echo %CYAN%[INFO]%NC% Criando commit...
    git commit -m "%MSG%"
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Falha ao criar commit.
        pause
        exit /b 1
    )
    echo %GREEN%[OK]%NC% Commit criado.
)

echo %CYAN%[INFO]%NC% Enviando para origin/%BRANCH%...
git push origin "%BRANCH%"
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha ao enviar. Tente fazer pull primeiro.
    echo %CYAN%[INFO]%NC% Executando pull com rebase...
    git pull --rebase --autostash
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Conflito no rebase. Resolva manualmente.
        pause
        exit /b 1
    )
    git push origin "%BRANCH%"
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Falha novamente no push.
        pause
        exit /b 1
    )
)
echo %GREEN%[OK]%NC% Push concluido com sucesso!
pause
exit /b 0

REM ============================================================
REM  FULL — Pull + Push
REM ============================================================
:full
echo %CYAN%[INFO]%NC% Branch: %BRANCH%
echo.
echo %CYAN%=== 1/2: Baixando atualizacoes remotas ===%NC%
call :sync_pull
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha no pull. Abortando.
    pause
    exit /b 1
)
echo.
echo %CYAN%=== 2/2: Enviando mudancas locais ===%NC%
call :push_internal
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha no push.
    pause
    exit /b 1
)
echo.
echo %GREEN%[OK]%NC% Sincronizacao completa (pull + push)!
pause
exit /b 0

REM ============================================================
REM  Funcao: sync_pull — Baixa atualizacoes remotas
REM ============================================================
:sync_pull
echo %CYAN%[INFO]%NC% Verificando atualizacoes em origin/%BRANCH%...

git fetch origin 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Falha ao fazer fetch.
    exit /b 1
)

REM --- Comparar branch atual com a remota ---
for /f "delims=" %%L in ('git rev-parse HEAD 2^>nul') do set "LOCAL=%%L"
for /f "delims=" %%R in ('git rev-parse "origin/%BRANCH%" 2^>nul') do set "REMOTE=%%R"

if "%LOCAL%"=="%REMOTE%" (
    echo %GREEN%[OK]%NC% Ja esta atualizado.
    exit /b 0
)

echo %CYAN%[INFO]%NC% Novas atualizacoes detectadas! Baixando...
git pull --rebase --autostash
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Erro ao atualizar. Conflitos podem existir.
    echo %YELLOW%[WARN]%NC% Se houver conflitos, resolva-os e rode: git rebase --continue
    exit /b 1
)

echo %GREEN%[OK]%NC% Sincronizado com sucesso!
exit /b 0

REM ============================================================
REM  Funcao: push_internal — Envia sem interacao
REM ============================================================
:push_internal
git diff --quiet --exit-code >nul 2>&1
set "HAS_UNSTAGED=%errorlevel%"
git diff --cached --quiet --exit-code >nul 2>&1
set "HAS_STAGED=%errorlevel%"

REM --- Verificar arquivos nao rastreados (untracked) ---
git ls-files --others --exclude-standard 2>nul | findstr /r "." >nul 2>&1
set "HAS_UNTRACKED=%errorlevel%"

REM --- Se nao ha mudancas nem untracked, apenas faz push dos commits ---
if "%HAS_UNSTAGED%"=="0" if "%HAS_STAGED%"=="0" if "%HAS_UNTRACKED%"=="1" (
    git log origin/%BRANCH%..HEAD --oneline 2>nul | findstr /r "." >nul 2>&1
    if errorlevel 1 (
        echo %GREEN%[OK]%NC% Nada para enviar.
        exit /b 0
    )
    git push origin "%BRANCH%"
    if errorlevel 1 (
        exit /b 1
    )
    exit /b 0
)

REM --- Ha mudancas: adicionar e commitar ---
git add -A
git commit -m "Sync automatico %DATE% %TIME%" >nul 2>&1
git push origin "%BRANCH%"
if errorlevel 1 (
    git pull --rebase --autostash >nul 2>&1
    git push origin "%BRANCH%"
    if errorlevel 1 (
        exit /b 1
    )
)
exit /b 0

REM ============================================================
REM  Fim do script — pause de seguranca
REM ============================================================
:end
echo.
echo %CYAN%[INFO]%NC% Script finalizado.
pause
exit /b 0
