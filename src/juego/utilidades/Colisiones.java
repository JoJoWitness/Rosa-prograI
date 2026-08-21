package juego.utilidades;

import java.util.List;

import juego.objetos.Jugador;
import juego.objetos.Plataforma;

public class Colisiones {

    public static void mover(Jugador j, char[][] mapa) {
        mover(j, mapa, null);
    }

    // Un solo metodo para los dos jugadores: si se duplica, se desincronizan
    // en la primera correccion.
    public static void mover(Jugador j, char[][] mapa, List<Plataforma> plataformas) {
        j.enSuelo = false;

        j.velY += Constantes.GRAVEDAD;
        if (j.velY > Constantes.VEL_MAX_CAIDA) {
            j.velY = Constantes.VEL_MAX_CAIDA;
        }

        moverEnX(j, mapa);
        moverEnY(j, mapa);
        chocarPlataformasEnY(j, plataformas);
    }

    // Las plataformas colgantes no estan en el mapa, se revisan aparte. El
    // tablon esta girado, asi que no es un rectangulo: solo frena a quien cae
    // desde arriba, y por debajo y por los lados se atraviesa. Se mira el
    // cruce del frame y no el solapamiento, o cayendo a VEL_MAX_CAIDA se
    // colaria de largo sin llegar a tocarlo.
    private static void chocarPlataformasEnY(Jugador j, List<Plataforma> plataformas) {
        if (plataformas == null || j.velY <= 0) {
            return;
        }
        for (Plataforma p : plataformas) {
            if (!p.sujeta()) {
                continue;                     // volcada: no sostiene a nadie
            }
            // Basta con que el cuerpo pise el tablon; no hace falta que lo
            // pise el centro. Exigiendo el centro, al caer en una esquina el
            // cuerpo apoyaba pero el centro quedaba fuera y se colaba de largo.
            if (j.x + j.ancho <= p.x || j.x >= p.x + p.ancho) {
                continue;
            }

            int sup = p.alturaEn(muestra(j, p));
            int pies = j.y + j.alto;

            // Con margen a los dos lados: sacar los pies de dentro de la
            // tabla si se hundieron, y bajarlos a ella si se quedaron en el
            // aire. Solo mientras el tablon sujete; pasada ANG_CAIDA suelta.
            if (pies - j.velY <= sup + Constantes.PEGADO_TABLON
                && pies >= sup - Constantes.PEGADO_TABLON) {
                j.y = sup - j.alto;
                j.velY = 0;
                j.enSuelo = true;
            }
        }
    }

    // Donde se mide la altura del tablon: el centro del jugador, pero sin
    // salirse de el. Cayendo en la punta, el centro esta fuera y hay que medir
    // en el extremo o saldria una altura inventada por la pendiente.
    private static int muestra(Jugador j, Plataforma p) {
        int centro = j.x + j.ancho / 2;
        return Math.max(p.x, Math.min(centro, p.x + p.ancho - 1));
    }

    private static void moverEnX(Jugador j, char[][] mapa) {
        if (j.velX == 0) {
            return;
        }

        j.x += j.velX;

        if (j.velX > 0) {
            int col = celda(j.x + j.ancho - 1);
            if (columnaChoca(j, mapa, col)) {
                j.x = col * Constantes.CELDA - j.ancho;
                j.velX = 0;
            }
        } else {
            int col = celda(j.x);
            if (columnaChoca(j, mapa, col)) {
                j.x = (col + 1) * Constantes.CELDA;
                j.velX = 0;
            }
        }
    }

    private static void moverEnY(Jugador j, char[][] mapa) {
        if (j.velY == 0) {
            return;
        }

        j.y += j.velY;

        if (j.velY > 0) {
            int fila = celda(j.y + j.alto - 1);
            if (filaChoca(j, mapa, fila)) {
                j.y = fila * Constantes.CELDA - j.alto;
                j.velY = 0;
                j.enSuelo = true;
            }
        } else {
            int fila = celda(j.y);
            if (filaChoca(j, mapa, fila)) {
                j.y = (fila + 1) * Constantes.CELDA;
                j.velY = 0;
            }
        }
    }

    // Solo se revisan las celdas que ocupa el jugador, no el nivel entero.
    private static boolean columnaChoca(Jugador j, char[][] mapa, int col) {
        int filaIni = celda(j.y);
        int filaFin = celda(j.y + j.alto - 1);
        for (int fila = filaIni; fila <= filaFin; fila++) {
            if (esSolido(mapa, fila, col)) {
                return true;
            }
        }
        return false;
    }

    private static boolean filaChoca(Jugador j, char[][] mapa, int fila) {
        int colIni = celda(j.x);
        int colFin = celda(j.x + j.ancho - 1);
        for (int col = colIni; col <= colFin; col++) {
            if (esSolido(mapa, fila, col)) {
                return true;
            }
        }
        return false;
    }

    private static boolean esSolido(char[][] mapa, int fila, int col) {
        if (fila < 0 || col < 0 || fila >= mapa.length || col >= mapa[0].length) {
            return true;
        }
        return mapa[fila][col] == Constantes.BLOQUE;
    }

    // floorDiv y no "/" para que las posiciones negativas den la celda correcta.
    private static int celda(int pixeles) {
        return Math.floorDiv(pixeles, Constantes.CELDA);
    }

    private Colisiones() {
    }
}
