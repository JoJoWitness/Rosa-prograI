#!/bin/sh
# Empaqueta el juego en un solo archivo, Luz-y-Sombra.jar, con las imagenes y
# los niveles dentro. Se abre con doble clic o con: java -jar Luz-y-Sombra.jar
cd "$(dirname "$0")" || exit 1
./compilar.sh >/dev/null || exit 1
jar --create --file Luz-y-Sombra.jar --main-class juego.Main -C bin . assets || exit 1
echo "Escrito Luz-y-Sombra.jar"
