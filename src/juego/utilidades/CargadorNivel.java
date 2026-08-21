package juego.utilidades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import juego.objetos.Elemento;
import juego.objetos.Nivel;
import juego.objetos.Plataforma;

public class CargadorNivel {

    // Tipos que se juntan cuando aparecen en celdas seguidas de la misma fila.
    private static final String UNIBLES = "^ABEeFfmn";

    public static Nivel cargar(String ruta, String rutaFondo) {
        List<String> lineas = leerLineas(ruta);

        Nivel nivel = new Nivel(Constantes.FILAS, Constantes.COLUMNAS);
        nivel.ruta = ruta;
        nivel.rutaFondo = rutaFondo;

        char[][] simbolos = new char[Constantes.FILAS][Constantes.COLUMNAS];

        for (int fila = 0; fila < Constantes.FILAS; fila++) {
            String linea = fila < lineas.size() ? lineas.get(fila) : "";

            for (int col = 0; col < Constantes.COLUMNAS; col++) {
                char c = col < linea.length() ? linea.charAt(col) : Constantes.VACIO;
                simbolos[fila][col] = c;

                // Todo lo que no es bloque deja la celda vacia: si no, los charcos
                // y los coleccionables serian solidos.
                nivel.mapa[fila][col] = (c == Constantes.BLOQUE) ? Constantes.BLOQUE : Constantes.VACIO;
            }
        }

        construirElementos(nivel, simbolos);
        atarPolea(nivel);
        return nivel;
    }

    private static void construirElementos(Nivel nivel, char[][] simbolos) {
        for (int fila = 0; fila < Constantes.FILAS; fila++) {
            for (int col = 0; col < Constantes.COLUMNAS; col++) {
                char c = simbolos[fila][col];

                if (c == Constantes.VACIO || c == Constantes.BLOQUE) {
                    continue;
                }

                if (c == Constantes.INICIO_LUZ) {
                    nivel.luzX = xJugadorEnCelda(col);
                    nivel.luzY = yJugadorEnCelda(fila);
                    continue;
                }
                if (c == Constantes.INICIO_SOMBRA) {
                    nivel.sombraX = xJugadorEnCelda(col);
                    nivel.sombraY = yJugadorEnCelda(fila);
                    continue;
                }

                int celdas = 1;
                if (UNIBLES.indexOf(c) >= 0) {
                    while (col + celdas < Constantes.COLUMNAS && simbolos[fila][col + celdas] == c) {
                        celdas++;
                    }
                }

                if (Constantes.esMuroQueCruza(c)) {
                    // Sale de un pilar, y al activarse aparece al otro lado de
                    // el. El pilar es el bloque pegado a la tira, por un lado o
                    // por el otro; cada celda se refleja respecto a el.
                    int fin = col + celdas - 1;
                    int pilar = (fin + 1 < Constantes.COLUMNAS
                                 && simbolos[fila][fin + 1] == Constantes.BLOQUE)
                                ? fin + 1 : col - 1;

                    for (int k = col; k <= fin; k++) {
                        nivel.mapa[fila][k] = Constantes.BLOQUE;   // arranca en reposo
                        nivel.murosCruzan.add(new int[] {
                            fila, k, pilar + (pilar - k), Constantes.grupoDe(c) });
                    }
                } else if (Constantes.esMuro(c)) {
                    // arranca cerrado: solido en el mapa hasta que lo abran
                    nivel.mapa[fila][col] = Constantes.BLOQUE;
                    nivel.muros.add(new int[] { fila, col, Constantes.grupoDe(c) });
                } else if (Constantes.esColgante(c)) {
                    nivel.plataformas.add(new Plataforma(c, fila, col, celdas));
                } else {
                    nivel.elementos.add(new Elemento(c, fila, col, celdas));

                    if (c == Constantes.ALBOR) {
                        nivel.totalAlbores++;
                    } else if (c == Constantes.OBSIDIANA) {
                        nivel.totalObsidianas++;
                    }
                }

                col += celdas - 1;
            }
        }
    }

    /**
     * Ata los dos extremos de la cuerda de la polea. Se busca una tira de 'e'
     * y una de 'f': el nivel 2 lleva una sola cuerda, la que cruza el techo en
     * 'docs/3.png', asi que sobra con emparejar el primero de cada clase. Un
     * extremo suelto se queda sin pareja y sube y baja el solo.
     */
    private static void atarPolea(Nivel nivel) {
        Plataforma baja = null;
        Plataforma sube = null;

        for (Plataforma p : nivel.plataformas) {
            if (!p.polea) {
                continue;
            }
            if (p.sentidoBase > 0 && baja == null) {
                baja = p;
            } else if (p.sentidoBase < 0 && sube == null) {
                sube = p;
            }
        }

        if (baja != null && sube != null) {
            baja.pareja = sube;
            sube.pareja = baja;
        }
    }

    // El jugador queda centrado en la celda y con los pies en su borde inferior.
    private static int xJugadorEnCelda(int columna) {
        return columna * Constantes.CELDA + (Constantes.CELDA - Constantes.ANCHO_JUGADOR) / 2;
    }

    private static int yJugadorEnCelda(int fila) {
        return (fila + 1) * Constantes.CELDA - Constantes.ALTO_JUGADOR;
    }

    private static List<String> leerLineas(String ruta) {
        try (InputStream entrada = Assets.abrir(ruta)) {
            if (entrada != null) {
                BufferedReader lector =
                    new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
                List<String> lineas = new ArrayList<String>();
                for (String linea = lector.readLine(); linea != null; linea = lector.readLine()) {
                    lineas.add(linea);
                }
                return lineas;
            }
        } catch (IOException e) {
            // se queda con el nivel de reserva
        }
        System.out.println("No se pudo leer " + Assets.archivo(ruta).getAbsolutePath()
                           + ", se usa el nivel de reserva.");
        return List.of(nivelDeReserva());
    }

    private static String[] nivelDeReserva() {
        String[] mapa = new String[Constantes.FILAS];
        for (int fila = 0; fila < Constantes.FILAS; fila++) {
            if (fila == 0 || fila == Constantes.FILAS - 1) {
                mapa[fila] = "#".repeat(Constantes.COLUMNAS);
            } else if (fila == Constantes.FILAS - 2) {
                mapa[fila] = "#..1..2" + ".".repeat(Constantes.COLUMNAS - 8) + "#";
            } else {
                mapa[fila] = "#" + ".".repeat(Constantes.COLUMNAS - 2) + "#";
            }
        }
        return mapa;
    }

    private CargadorNivel() {
    }
}
