import java.util.*;
import juego.objetos.*;
import juego.utilidades.*;

public class PruebaJugable {
    static final int C = Constantes.CELDA;
    static int fallos = 0;

    /** union de lo alcanzable con los mecanismos de esos grupos apagados y encendidos */
    public static Map<Long,int[]> alcance(Nivel nv, int tipo, int x0, int y0, boolean[] abiertos) {
        return new Alcanzable(nv, tipo, abiertos).explorar(x0, y0);
    }

    static boolean toca(Map<Long,int[]> alc, Elemento e) {
        for (int[] p : alc.values()) {
            java.awt.Rectangle r = new java.awt.Rectangle(
                p[0] + 8, p[1] + 8, Constantes.ANCHO_JUGADOR - 16, Constantes.ALTO_JUGADOR - 16);
            if (r.intersects(e.getRectangulo())) return true;
        }
        return false;
    }

    /**
     * Grupos que se pueden llegar a abrir: se empieza con todos los muros
     * puestos y se van sumando los grupos cuyo interruptor queda al alcance de
     * alguno de los dos, que a su vez abre camino hacia mas interruptores.
     */
    static boolean[] gruposAlcanzables(Nivel nv) {
        boolean[] abiertos = new boolean[Constantes.GRUPOS];
        while (true) {
            Map<Long,int[]> aL = alcance(nv, Jugador.LUZ, nv.luzX, nv.luzY, abiertos);
            Map<Long,int[]> aS = alcance(nv, Jugador.SOMBRA, nv.sombraX, nv.sombraY, abiertos);

            boolean nuevo = false;
            for (Elemento e : nv.elementos) {
                if (!Constantes.esBoton(e.tipo) && !Constantes.esPalanca(e.tipo)) continue;
                if (e.grupo < 0 || e.grupo >= abiertos.length || abiertos[e.grupo]) continue;
                if (toca(aL, e) || toca(aS, e)) { abiertos[e.grupo] = true; nuevo = true; }
            }
            if (!nuevo) return abiertos;
        }
    }

    public static boolean llegaAPub(int[] p, Elemento puerta) {
        int centro = p[0] + Constantes.ANCHO_JUGADOR / 2;
        int pies = p[1] + Constantes.ALTO_JUGADOR;
        return centro >= puerta.x && centro < puerta.x + C
            && pies > puerta.y && pies <= puerta.y + C;
    }

    static boolean llegaA(Map<Long,int[]> alc, Elemento puerta) {
        for (int[] p : alc.values()) {
            int centro = p[0] + Constantes.ANCHO_JUGADOR / 2;
            int pies = p[1] + Constantes.ALTO_JUGADOR;
            if (centro >= puerta.x && centro < puerta.x + C
                && pies > puerta.y && pies <= puerta.y + C) return true;
        }
        return false;
    }

    /**
     * Cristales que se pueden coger de verdad. No basta con mirar las posturas
     * de reposo: casi todos se rozan en el aire. Se repiten desde cada postura
     * los mismos saltos que explora {@link Alcanzable} y solo cuenta lo tocado
     * en un salto del que se vuelve vivo - si acaba en un charco, el nivel se
     * reinicia y el cristal no se queda.
     */
    static Set<Elemento> recogibles(Nivel nv, int tipo, Map<Long,int[]> alc, boolean[] abiertos) {
        Alcanzable al = new Alcanzable(nv, tipo, abiertos);
        Set<Elemento> tocados = new HashSet<Elemento>();
        for (int[] p : alc.values()) {
            marcar(nv, tocados, p[0], p[1]);
            for (int v = 0; v < 2; v++) {
                for (int dir = -1; dir <= 1; dir++) {
                    for (int salta = 0; salta <= 1; salta++) {
                        if (dir == 0 && salta == 0) continue;
                        for (int hold : Alcanzable.MANTENER) {
                            vuelo(al, nv, tocados, p[0], p[1], dir, salta == 1, hold, v, tipo);
                        }
                    }
                }
            }
        }
        return tocados;
    }

    static void vuelo(Alcanzable al, Nivel nv, Set<Elemento> tocados,
                      int x, int y, int dir, boolean salta, int hold, int v, int tipo) {
        Set<Elemento> enVuelo = new HashSet<Elemento>();
        Jugador j = new Jugador(tipo, x, y);
        j.enSuelo = true;
        if (salta) j.saltar();
        for (int i = 0; i < 260; i++) {
            j.velX = (i < hold) ? dir * Constantes.VEL_X : 0;
            Colisiones.mover(j, al.mapas[v], al.plataformas.get(v));
            if (al.mortal(j)) return;
            marcar(nv, enVuelo, j.x, j.y);
            if (j.enSuelo && i >= 2) { tocados.addAll(enVuelo); return; }
        }
    }

    static void marcar(Nivel nv, Set<Elemento> tocados, int x, int y) {
        java.awt.Rectangle r = new java.awt.Rectangle(x + 8, y + 8,
            Constantes.ANCHO_JUGADOR - 16, Constantes.ALTO_JUGADOR - 16);
        for (Elemento e : nv.elementos) {
            if ((e.tipo == Constantes.ALBOR || e.tipo == Constantes.OBSIDIANA)
                && r.intersects(e.getRectangulo())) {
                tocados.add(e);
            }
        }
    }

    static int cuenta(Set<Elemento> tocados, char tipo) {
        int n = 0;
        for (Elemento e : tocados) if (e.tipo == tipo) n++;
        return n;
    }

    static Elemento buscar(Nivel nv, char t) {
        for (Elemento e : nv.elementos) if (e.tipo == t) return e;
        return null;
    }

    public static void main(String[] a) {
        for (int i = 1; i <= 4; i++) {
            Nivel nv = CargadorNivel.cargar("assets/niveles/nivel" + i + ".txt", "");
            System.out.println("--- nivel " + i + " ---");

            boolean[] abiertos = gruposAlcanzables(nv);
            Map<Long,int[]> aL = alcance(nv, Jugador.LUZ, nv.luzX, nv.luzY, abiertos);
            Map<Long,int[]> aS = alcance(nv, Jugador.SOMBRA, nv.sombraX, nv.sombraY, abiertos);

            for (int g = 0; g < Constantes.GRUPOS; g++) {
                if (abiertos[g]) continue;
                for (int[] m : nv.muros) {
                    if (m[2] != g) continue;
                    System.out.println("  aviso: nadie llega al interruptor del grupo " + g
                                       + ", asi que su muro no se abre nunca");
                    break;
                }
            }

            Elemento pL = buscar(nv, Constantes.PUERTA_LUZ);
            Elemento pS = buscar(nv, Constantes.PUERTA_SOMBRA);

            boolean okL = pL != null && llegaA(aL, pL);
            boolean okS = pS != null && llegaA(aS, pS);

            Set<Elemento> tocaL = recogibles(nv, Jugador.LUZ, aL, abiertos);
            Set<Elemento> tocaS = recogibles(nv, Jugador.SOMBRA, aS, abiertos);
            int albores = cuenta(tocaL, Constantes.ALBOR);
            int obsidianas = cuenta(tocaS, Constantes.OBSIDIANA);

            System.out.printf("  Luz    : %5d posiciones | puerta en (%2d,%2d) -> %s | albores %d/%d%n",
                aL.size(), pL == null ? -1 : pL.x / C, pL == null ? -1 : pL.y / C,
                okL ? "LLEGA" : "NO LLEGA", albores, nv.totalAlbores);
            System.out.printf("  Sombra : %5d posiciones | puerta en (%2d,%2d) -> %s | obsidianas %d/%d%n",
                aS.size(), pS == null ? -1 : pS.x / C, pS == null ? -1 : pS.y / C,
                okS ? "LLEGA" : "NO LLEGA", obsidianas, nv.totalObsidianas);

            for (Elemento e : nv.elementos) {
                boolean albor = e.tipo == Constantes.ALBOR;
                if (!albor && e.tipo != Constantes.OBSIDIANA) continue;
                if ((albor ? tocaL : tocaS).contains(e)) continue;
                System.out.printf("  no se puede coger %s de la fila %d, columna %d%n",
                    albor ? "el albor" : "la obsidiana", e.y / C, e.x / C);
            }

            if (!okL || !okS
                || albores < nv.totalAlbores || obsidianas < nv.totalObsidianas) fallos++;
        }
        System.out.println(fallos == 0 ? "\nLOS 4 NIVELES SON COMPLETABLES"
                                       : "\n" + fallos + " NIVEL(ES) CON PEGAS");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
