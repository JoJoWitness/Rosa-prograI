#!/bin/sh
# Ejecuta el juego. Las rutas de assets/ son relativas a la raiz del proyecto.
cd "$(dirname "$0")" || exit 1
# Se compila siempre, para no arrastrar clases viejas.
./compilar.sh >/dev/null || exit 1
java -cp bin juego.Main
