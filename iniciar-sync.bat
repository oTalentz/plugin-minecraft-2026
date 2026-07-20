@echo off
REM iniciar-sync.bat — Inicia sincronizacao automatica (5s)
cd /d "%~dp0"
echo ========================================
echo   Sincronizacao Automatica do Git
echo   Intervalo: 5 segundos
echo   Pressione Ctrl+C para parar
echo ========================================
echo.
call scripts\sync-git.bat monitor
pause
