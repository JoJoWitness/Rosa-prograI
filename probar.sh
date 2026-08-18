#!/bin/sh
# Compila y ejecuta todas las pruebas. Sin ventana: se puede lanzar en cualquier sitio.
cd "$(dirname "$0")" || exit 1
./compilar.sh >/dev/null || exit 1
mkdir -p bin-pruebas
javac -cp bin -d bin-pruebas pruebas/*.java || exit 1
CP="bin:bin-pruebas"
D=pruebas/datos
fallos=0
ejecutar() {
  printf "%-18s " "$1"; shift
  if salida=$("$@" 2>&1); then echo "$salida" | tail -1
  else echo "$salida" | tail -1; fallos=$((fallos+1)); fi
}
ejecutar PruebaTeclado    java -cp "$CP" PruebaTeclado
ejecutar PruebaRaton      java -cp "$CP" PruebaRaton
ejecutar PruebaFisica     java -Dnivel=$D/pruebas.txt -cp "$CP" PruebaFisica
ejecutar PruebaReglas     java -Dnivel=$D/pruebas.txt -cp "$CP" PruebaReglas
ejecutar PruebaMecanismos java -cp "$CP" PruebaMecanismos $D/mecanismos.txt
ejecutar PruebaNiveles    java -cp "$CP" PruebaNiveles
ejecutar PruebaSalto      java -cp "$CP" PruebaSalto $D
ejecutar PruebaJugable    java -Xss16m -cp "$CP" PruebaJugable
ejecutar AutoJugador      java -cp "$CP" AutoJugador
ejecutar PruebaPartida    java -Xss64m -cp "$CP" PruebaPartida
[ $fallos -eq 0 ] && echo "\nTODO CORRECTO" || echo "\n$fallos SUITE(S) CON FALLOS"
exit $fallos
