#!/bin/sh
# Compila y ejecuta todas las pruebas. Sin ventana: se puede lanzar en cualquier sitio.
cd "$(dirname "$0")" || exit 1
./compilar.sh >/dev/null || exit 1
mkdir -p bin-pruebas
javac -cp bin -d bin-pruebas pruebas/*.java || exit 1
CP="bin:bin-pruebas"
D=pruebas/datos
fallos=0
# Las pruebas van mudas: no hay que oir sesenta palancas seguidas, y en una
# maquina sin tarjeta de sonido abrir el Clip cuesta un rato.
SIN_SONIDO=-DsinSonido=1
ejecutar() {
  printf "%-18s " "$1"; shift
  if salida=$("$@" 2>&1); then echo "$salida" | tail -1
  else echo "$salida" | tail -1; fallos=$((fallos+1)); fi
}
ejecutar PruebaTeclado    java $SIN_SONIDO -cp "$CP" PruebaTeclado
ejecutar PruebaRaton      java $SIN_SONIDO -cp "$CP" PruebaRaton
ejecutar PruebaFisica     java $SIN_SONIDO -Dnivel=$D/pruebas.txt -cp "$CP" PruebaFisica
ejecutar PruebaReglas     java $SIN_SONIDO -Dnivel=$D/pruebas.txt -cp "$CP" PruebaReglas
ejecutar PruebaMecanismos java $SIN_SONIDO -cp "$CP" PruebaMecanismos $D/mecanismos.txt $D/polea.txt
ejecutar PruebaNiveles    java $SIN_SONIDO -cp "$CP" PruebaNiveles
ejecutar PruebaSalto      java $SIN_SONIDO -cp "$CP" PruebaSalto $D
ejecutar PruebaJugable    java $SIN_SONIDO -Xss16m -cp "$CP" PruebaJugable
ejecutar AutoJugador      java $SIN_SONIDO -cp "$CP" AutoJugador
ejecutar PruebaPartida    java $SIN_SONIDO -Xss64m -cp "$CP" PruebaPartida
[ $fallos -eq 0 ] && echo "\nTODO CORRECTO" || echo "\n$fallos SUITE(S) CON FALLOS"
exit $fallos
