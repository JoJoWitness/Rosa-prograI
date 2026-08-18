@echo off
rem Compila y ejecuta todas las pruebas  (equivalente de probar.sh)
rem Sin ventana: se puede lanzar en cualquier sitio.
setlocal enabledelayedexpansion
cd /d "%~dp0"

call "%~dp0compilar.bat" >nul
if errorlevel 1 exit /b 1

if not exist bin-pruebas mkdir bin-pruebas
javac -encoding UTF-8 -cp bin -d bin-pruebas pruebas\*.java
if errorlevel 1 exit /b 1

rem En Windows el separador del classpath es ; y no :
set "CP=bin;bin-pruebas"
set "D=pruebas/datos"
set "SAL=%TEMP%\luzysombra-prueba.txt"
set /a fallos=0

set "CMD=java -cp %CP% PruebaTeclado"
call :correr PruebaTeclado
set "CMD=java -cp %CP% PruebaRaton"
call :correr PruebaRaton
set "CMD=java -Dnivel=%D%/pruebas.txt -cp %CP% PruebaFisica"
call :correr PruebaFisica
set "CMD=java -Dnivel=%D%/pruebas.txt -cp %CP% PruebaReglas"
call :correr PruebaReglas
set "CMD=java -cp %CP% PruebaMecanismos %D%/mecanismos.txt"
call :correr PruebaMecanismos
set "CMD=java -cp %CP% PruebaNiveles"
call :correr PruebaNiveles
set "CMD=java -cp %CP% PruebaSalto %D%"
call :correr PruebaSalto
set "CMD=java -Xss16m -cp %CP% PruebaJugable"
call :correr PruebaJugable
set "CMD=java -cp %CP% AutoJugador"
call :correr AutoJugador
set "CMD=java -Xss64m -cp %CP% PruebaPartida"
call :correr PruebaPartida

if exist "%SAL%" del "%SAL%"
echo.
if %fallos%==0 (
    echo TODO CORRECTO
) else (
    echo %fallos% SUITE^(S^) CON FALLOS
)
exit /b %fallos%

rem ---------------------------------------------------------------------------
rem Lanza %CMD%, imprime el nombre y la ultima linea que haya escrito, y cuenta
rem el fallo si termino mal.
:correr
%CMD% > "%SAL%" 2>&1
set "COD=%ERRORLEVEL%"
set "ULTIMA="
for /f "usebackq eol=| delims=" %%l in ("%SAL%") do set "ULTIMA=%%l"
set "NOM=%~1                  "
echo !NOM:~0,18! !ULTIMA!
if not "%COD%"=="0" set /a fallos+=1
exit /b
