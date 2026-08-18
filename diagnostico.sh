#!/bin/sh
# Dice donde busca el juego sus archivos y si los encuentra.
cd "$(dirname "$0")" || exit 1
./compilar.sh >/dev/null || exit 1
java -cp bin juego.Diagnostico
