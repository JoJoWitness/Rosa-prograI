#!/bin/sh
# Compila todo el codigo fuente en bin/
cd "$(dirname "$0")" || exit 1
mkdir -p bin
javac -d bin $(find src -name '*.java') && echo "Compilado en bin/"
