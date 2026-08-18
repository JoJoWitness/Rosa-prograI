@echo off
rem Dice donde busca el juego sus archivos y si los encuentra.
rem Lanzar esto y mandar lo que salga cuando el juego se vea en blanco.
setlocal
cd /d "%~dp0"

call "%~dp0compilar.bat"
if errorlevel 1 exit /b 1

echo.
java -cp bin juego.Diagnostico
echo.
pause
