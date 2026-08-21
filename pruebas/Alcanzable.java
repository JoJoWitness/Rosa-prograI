import java.awt.Rectangle;
import java.util.*;
import juego.objetos.*;
import juego.utilidades.*;

/**
 * Busca, con la fisica real del juego, a donde puede llegar un jugador.
 * En cada paso prueba las dos configuraciones de los mecanismos: es el modelo
 * de que el otro jugador puede estar pisando un boton o no.
 *
 * Solo se abren los grupos que se le pasan en {@code abiertos}: un muro cuyo
 * interruptor no alcanza nadie no se puede quitar, y darlo por abierto era lo
 * que dejaba pasar por bueno un nivel imposible.
 */
public class Alcanzable {

    static final int C = Constantes.CELDA;
    /** Cuantos frames se mantiene apretada la direccion en cada tanteo. */
    static final int[] MANTENER = { 4, 8, 12, 16, 20, 26, 34, 45, 60, 90, 160 };
    static final int BIN = 8;                 // cuantizacion de x
    static final int MARGEN = 8;              // igual que ControladorNivel

    final Nivel nivel;
    final int tipo;
    final char[][][] mapas;                   // [cerrado, abierto]
    /** Los tablones colgantes, planos: nadie llama a girar() desde aqui. */
    final List<Plataforma> plataformas;

    Alcanzable(Nivel nivel, int tipo, boolean[] abiertos) {
        this.nivel = nivel;
        this.tipo = tipo;
        this.mapas = new char[2][][];
        this.plataformas = nivel.plataformas;

        for (int v = 0; v < 2; v++) {
            char[][] m = new char[nivel.filas][];
            for (int f = 0; f < nivel.filas; f++) {
                m[f] = nivel.mapa[f].clone();
            }
            for (int[] mu : nivel.muros) {
                boolean seQuita = v == 1 && Constantes.muroQuitado(mu[2], abiertos);
                m[mu[0]][mu[1]] = seQuita ? Constantes.VACIO : Constantes.BLOQUE;
            }
            for (int[] mu : nivel.murosCruzan) {      // no se quita: cambia de lado
                boolean cruza = v == 1 && Constantes.muroQuitado(mu[3], abiertos);
                m[mu[0]][mu[1]] = cruza ? Constantes.VACIO : Constantes.BLOQUE;
                m[mu[0]][mu[2]] = cruza ? Constantes.BLOQUE : Constantes.VACIO;
            }
            mapas[v] = m;
        }
    }

    boolean mortal(Jugador j) {
        Rectangle cuerpo = j.getRectangulo(MARGEN);
        for (Elemento e : nivel.elementos) {
            if (!cuerpo.intersects(e.getRectangulo())) continue;
            if (e.tipo == Constantes.PUAS) return true;
            if (e.tipo == Constantes.LUMINOSIDAD && tipo == Jugador.SOMBRA) return true;
            if (e.tipo == Constantes.PENUMBRA && tipo == Jugador.LUZ) return true;
        }
        return false;
    }

    static long clave(int x, int y) { return ((long)(x / BIN) << 20) | (y & 0xFFFFF); }

    /** Todos los estados de reposo alcanzables desde el inicio. */
    Map<Long,int[]> explorar(int x0, int y0) {
        Map<Long,int[]> vistos = new HashMap<Long,int[]>();
        Deque<int[]> cola = new ArrayDeque<int[]>();

        for (int v = 0; v < 2; v++) {
            int[] inicio = asentar(x0, y0, v);
            if (inicio == null) continue;
            long k = clave(inicio[0], inicio[1]);
            if (!vistos.containsKey(k)) { vistos.put(k, inicio); cola.add(inicio); }
        }

        int[] dirs = { -1, 0, 1 };
        int[] mantener = MANTENER;

        while (!cola.isEmpty()) {
            int[] p = cola.poll();
            for (int v = 0; v < 2; v++) {
                for (int dir : dirs) {
                    for (int salta = 0; salta <= 1; salta++) {
                        if (dir == 0 && salta == 0) continue;
                        for (int hold : mantener) {
                            int[] fin = simular(p[0], p[1], dir, salta == 1, hold, v);
                            if (fin == null) continue;
                            long k = clave(fin[0], fin[1]);
                            if (!vistos.containsKey(k)) { vistos.put(k, fin); cola.add(fin); }
                        }
                    }
                }
            }
        }
        return vistos;
    }

    Jugador nuevo(int x, int y) {
        Jugador j = new Jugador(tipo, x, y);
        j.velX = 0; j.velY = 0; j.enSuelo = true;
        return j;
    }

    /** Deja caer al jugador hasta que se apoye; null si muere o no se apoya. */
    int[] asentar(int x, int y, int v) {
        Jugador j = nuevo(x, y);
        for (int i = 0; i < 300; i++) {
            Colisiones.mover(j, mapas[v], plataformas);
            if (mortal(j)) return null;
            if (j.enSuelo) return new int[] { j.x, j.y };
        }
        return null;
    }

    int[] simular(int x, int y, int dir, boolean salta, int hold, int v) {
        Jugador j = nuevo(x, y);
        if (salta) j.saltar();
        for (int i = 0; i < 260; i++) {
            j.velX = (i < hold) ? dir * Constantes.VEL_X : 0;
            Colisiones.mover(j, mapas[v], plataformas);
            if (mortal(j)) return null;
            if (j.enSuelo && i >= 2) {
                if (j.x == x && j.y == y) return null;
                return new int[] { j.x, j.y };
            }
        }
        return null;
    }
}
