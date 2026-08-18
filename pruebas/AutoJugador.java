import java.awt.Rectangle;
import java.util.*;
import juego.objetos.*;
import juego.utilidades.*;

/** Juega solo: avanza en una direccion y salta al ver peligro o vacio delante. */
public class AutoJugador {
    static final int C = Constantes.CELDA;

    final Nivel nivel; final int tipo; final char[] letales;
    public AutoJugador(Nivel nivel, int tipo) {
        this.nivel = nivel; this.tipo = tipo;
        this.letales = tipo == Jugador.LUZ ? new char[]{'B','^'} : new char[]{'A','^'};
    }

    boolean letalEn(Rectangle r) {
        for (Elemento e : nivel.elementos)
            for (char L : letales)
                if (e.tipo == L && r.intersects(e.getRectangulo())) return true;
        return false;
    }

    boolean solido(int px, int py) {
        int f = Math.floorDiv(py, C), c = Math.floorDiv(px, C);
        if (f < 0 || c < 0 || f >= nivel.filas || c >= nivel.columnas) return true;
        return nivel.mapa[f][c] == Constantes.BLOQUE;
    }

    /** Andar hacia delante sin saltar: comprueba donde se acaba cayendo. */
    boolean caidaSegura(Jugador origen, int dir) {
        Jugador j = new Jugador(tipo, origen.x, origen.y);
        j.enSuelo = true;
        for (int i = 0; i < 120; i++) {
            j.velX = dir * Constantes.VEL_X;
            Colisiones.mover(j, nivel.mapa, nivel.plataformas);
            if (letalEn(j.getRectangulo(8))) return false;
            if (j.enSuelo && i >= 6) return true;
        }
        return true;
    }

    /** Prueba el salto por adelantado: si acaba en muerte, no se salta. */
    boolean saltoSeguro(Jugador origen, int dir) {
        Jugador j = new Jugador(tipo, origen.x, origen.y);
        j.enSuelo = true; j.saltar();
        for (int i = 0; i < 120; i++) {
            j.velX = dir * Constantes.VEL_X;
            Colisiones.mover(j, nivel.mapa, nivel.plataformas);
            if (letalEn(j.getRectangulo(8))) return false;
            if (j.enSuelo && i >= 2) return true;
        }
        return false;
    }

    /** Recorre en la direccion dada saltando cuando toca. Devuelve el recorrido. */
    public List<int[]> recorrer(int x0, int y0, int dir, int frames) {
        Jugador j = new Jugador(tipo, x0, y0);
        List<int[]> ruta = new ArrayList<int[]>();
        for (int i = 0; i < frames; i++) {
            j.velX = dir * Constantes.VEL_X;

            if (j.enSuelo) {
                int frente = dir > 0 ? j.x + j.ancho : j.x - 1;
                // peligro justo delante, a la altura del cuerpo
                Rectangle sonda = new Rectangle(Math.min(frente, frente + dir*C), j.y, C, j.alto);
                boolean peligro = letalEn(sonda);
                // solo se salta por peligro. Saltar en los bordes hace caer
                // mas lejos, que es justo lo contrario de lo que conviene.
                if (peligro) {
                    if (saltoSeguro(j, dir)) j.saltar();
                    else { j.velX = 0; return ruta; }   // no se mete donde no cabe
                } else if (!caidaSegura(j, dir)) {
                    j.velX = 0; return ruta;            // no se tira a un vacio mortal
                }
            }

            Colisiones.mover(j, nivel.mapa, nivel.plataformas);
            if (letalEn(j.getRectangulo(8))) { ruta.add(new int[]{j.x, j.y, -1}); return ruta; }
            ruta.add(new int[]{j.x, j.y, j.enSuelo ? 1 : 0});
        }
        return ruta;
    }

    public static void main(String[] a) {
        int fallos = 0;
        for (int n = 1; n <= 4; n++) {
        Nivel nv = CargadorNivel.cargar("assets/niveles/nivel" + n + ".txt", "");
        System.out.println("--- nivel " + n + " ---");
        for (int tipo = 0; tipo <= 1; tipo++) {
            AutoJugador auto = new AutoJugador(nv, tipo);
            int x = tipo == 0 ? nv.luzX : nv.sombraX, y = tipo == 0 ? nv.luzY : nv.sombraY;
            for (int dir = 1; dir >= -1; dir -= 2) {
                List<int[]> r = auto.recorrer(x, y, dir, 700);
                int[] fin = r.get(r.size()-1);
                System.out.printf("  %-7s hacia %-9s -> %s col %d fila %d%n",
                    tipo == 0 ? "LUZ" : "SOMBRA", dir > 0 ? "la derecha" : "la izquierda",
                    fin[2] == -1 ? "MUERE en" : "se para en", fin[0]/C,
                    (fin[1]+Constantes.ALTO_JUGADOR)/C);
                if (fin[2] == -1) fallos++;
            }
        }
        }
        System.out.println(fallos == 0
            ? "\nNINGUN PERSONAJE MUERE SIN PODER EVITARLO"
            : "\n" + fallos + " MUERTE(S) INEVITABLE(S)");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
