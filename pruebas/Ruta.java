import java.awt.Rectangle;
import java.util.*;
import juego.objetos.*;
import juego.utilidades.*;

/**
 * Busca una ruta concreta (lista de movimientos) con una configuracion FIJA de
 * mecanismos: sin suponer que el otro jugador este pisando nada.
 * Cada movimiento es {dir, salta, hold}, igual que los que simula Alcanzable.
 */
public class Ruta {
    // BIN = 1: la postura que se guarda es la de verdad, no una redondeada. Con
    // BIN = 8 el plan salia de una postura y se ejecutaba desde otra hasta 7 px
    // mas alla, y el error se acumulaba movimiento a movimiento.
    static final int BIN = 1, MARGEN = 8;
    static final int[] HOLDS = {4, 8, 12, 16, 20, 26, 34, 45, 60, 90, 160};

    final Nivel nivel;
    final int tipo;
    final char[][] mapa;
    final List<Plataforma> plataformas;

    public Ruta(Nivel nivel, int tipo, boolean[] abiertos) {
        this.nivel = nivel;
        this.tipo = tipo;
        this.mapa = new char[nivel.filas][];
        for (int f = 0; f < nivel.filas; f++) mapa[f] = nivel.mapa[f].clone();
        for (int[] mu : nivel.muros) {
            boolean quita = Constantes.muroQuitado(mu[2], abiertos);
            mapa[mu[0]][mu[1]] = quita ? Constantes.VACIO : Constantes.BLOQUE;
        }
        for (int[] mu : nivel.murosCruzan) {          // no se quita: cambia de lado
            boolean cruza = Constantes.muroQuitado(mu[3], abiertos);
            mapa[mu[0]][mu[1]] = cruza ? Constantes.VACIO : Constantes.BLOQUE;
            mapa[mu[0]][mu[2]] = cruza ? Constantes.BLOQUE : Constantes.VACIO;
        }
        this.plataformas = nivel.plataformas;             // en reposo: nadie las pisa
    }

    static long clave(int x, int y) { return ((long)(x / BIN) << 20) | (y & 0xFFFFF); }

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

    Jugador nuevo(int x, int y) {
        Jugador j = new Jugador(tipo, x, y);
        j.enSuelo = true;
        return j;
    }

    int[] asentar(int x, int y) {
        Jugador j = nuevo(x, y);
        for (int i = 0; i < 300; i++) {
            Colisiones.mover(j, mapa, plataformas);
            if (mortal(j)) return null;
            if (j.enSuelo) return new int[] { j.x, j.y };
        }
        return null;
    }

    int[] simular(int x, int y, int dir, boolean salta, int hold) {
        Jugador j = nuevo(x, y);
        if (salta) j.saltar();
        for (int i = 0; i < 260; i++) {
            j.velX = (i < hold) ? dir * Constantes.VEL_X : 0;
            Colisiones.mover(j, mapa, plataformas);
            if (mortal(j)) return null;
            if (j.enSuelo && i >= 2) {
                if (j.x == x && j.y == y) return null;
                return new int[] { j.x, j.y };
            }
        }
        return null;
    }

    /** true si en esa postura el cuerpo toca el elemento */
    static boolean toca(int[] p, Elemento e) {
        return new Rectangle(p[0] + MARGEN, p[1] + MARGEN,
                Constantes.ANCHO_JUGADOR - 2 * MARGEN,
                Constantes.ALTO_JUGADOR - 2 * MARGEN).intersects(e.getRectangulo());
    }

    static boolean enPuerta(int[] p, Elemento puerta) {
        int centro = p[0] + Constantes.ANCHO_JUGADOR / 2;
        int pies = p[1] + Constantes.ALTO_JUGADOR;
        return centro >= puerta.x && centro < puerta.x + Constantes.CELDA
            && pies > puerta.y && pies <= puerta.y + Constantes.CELDA;
    }

    // Resultado de explorar: todas las posturas y de donde sale cada una.
    Map<Long,int[]> pos;
    Map<Long,long[]> desde;                                    // clave -> {clavePrevia, dir, salta, hold}
    long inicial;
    boolean explorada;

    /** Recorre todo lo alcanzable desde (x0,y0) una sola vez. */
    public void explorar(int x0, int y0) {
        pos = new LinkedHashMap<Long,int[]>();   // en orden de descubrimiento: el primero que valga es el mas corto
        desde = new HashMap<Long,long[]>();
        explorada = true;

        int[] inicio = asentar(x0, y0);
        if (inicio == null) { inicial = Long.MIN_VALUE; return; }

        Deque<int[]> cola = new ArrayDeque<int[]>();
        inicial = clave(inicio[0], inicio[1]);
        pos.put(inicial, inicio);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            int[] p = cola.poll();
            long kp = clave(p[0], p[1]);
            for (int dir = -1; dir <= 1; dir++) {
                for (int salta = 0; salta <= 1; salta++) {
                    if (dir == 0 && salta == 0) continue;
                    for (int hold : HOLDS) {
                        int[] fin = simular(p[0], p[1], dir, salta == 1, hold);
                        if (fin == null) continue;
                        long k = clave(fin[0], fin[1]);
                        if (pos.containsKey(k)) continue;
                        pos.put(k, fin);
                        desde.put(k, new long[] { kp, dir, salta, hold });
                        cola.add(fin);
                    }
                }
            }
        }
    }

    /** Postura alcanzada que cumple la meta, o null. */
    public int[] postura(java.util.function.Predicate<int[]> meta) {
        for (int[] p : pos.values()) if (meta.test(p)) return p;
        return null;
    }

    /**
     * Camino (en movimientos {dir, salta, hold}) desde (x0,y0) hasta una postura
     * que cumpla la meta, o null si no hay.
     */
    public List<int[]> buscar(int x0, int y0, java.util.function.Predicate<int[]> meta) {
        if (!explorada) explorar(x0, y0);
        if (inicial == Long.MIN_VALUE) return null;
        for (Map.Entry<Long,int[]> e : pos.entrySet()) {
            if (meta.test(e.getValue())) return camino(desde, pos, e.getKey(), inicial);
        }
        return null;
    }

    static List<int[]> camino(Map<Long,long[]> desde, Map<Long,int[]> pos, long k, long k0) {
        LinkedList<int[]> ruta = new LinkedList<int[]>();
        while (k != k0) {
            long[] d = desde.get(k);
            ruta.addFirst(new int[] { (int) d[1], (int) d[2], (int) d[3] });
            k = d[0];
        }
        return ruta;
    }
}
