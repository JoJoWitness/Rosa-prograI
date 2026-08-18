@echo off
rem Empaqueta el juego en un solo archivo, Luz-y-Sombra.jar, con las imagenes y
rem los niveles dentro. Se abre con doble clic o con: java -jar Luz-y-Sombra.jar
setlocal
cd /d "%~dp0"

call "%~dp0compilar.bat" >nul
if errorlevel 1 exit /b 1

jar --create --file Luz-y-Sombra.jar --main-class juego.Main -C bin . assets
if errorlevel 1 exit /b 1

echo Escrito Luz-y-Sombra.jar
