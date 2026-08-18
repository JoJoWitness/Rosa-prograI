@echo off
rem Compila todo el codigo fuente en bin\  (equivalente de compilar.sh)
setlocal
cd /d "%~dp0"

if not exist bin mkdir bin

rem La lista de fuentes va a un archivo y se le pasa a javac con @, porque en
rem Windows la linea de comandos no aguanta las 20 rutas. Cada una entre
rem comillas, por si el proyecto cuelga de una carpeta con espacios.
> bin\fuentes.txt (for /r "%~dp0src" %%f in (*.java) do @echo "%%f")

javac -encoding UTF-8 -d bin @bin\fuentes.txt
set "COD=%ERRORLEVEL%"
del bin\fuentes.txt
if not "%COD%"=="0" exit /b %COD%

echo Compilado en bin\
