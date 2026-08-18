@echo off
rem Ejecuta el juego  (equivalente de ejecutar.sh)
setlocal
cd /d "%~dp0"

rem Se compila SIEMPRE. Comprobar solo si existe bin\juego\Main.class dejaba
rem correr clases viejas cuando se copiaba el proyecto con bin/ ya dentro.
call "%~dp0compilar.bat"
if errorlevel 1 exit /b 1

java -cp bin juego.Main
