import juego.controladores.ControladorNivel;
import juego.objetos.*;
import juego.utilidades.*;

public class PruebaNiveles {
    static int fallos = 0;
    static void ok(String n, boolean c, String d) {
        System.out.println((c?"  OK   ":"  FALLA")+"  "+n+"   "+d);
        if (!c) fallos++;
    }
    public static void main(String[] a) {
        for (int i = 1; i <= 4; i++) {
            System.out.println("--- nivel " + i + " ---");
            Nivel nv = CargadorNivel.cargar("assets/niveles/nivel"+i+".txt",
                                            "assets/fondos/nivel"+i+".png");
            ControladorNivel n = new ControladorNivel(nv);

            ok("hay spawn de Luz", nv.luzY != 0, "y=" + nv.luzY);
            ok("hay spawn de Sombra", nv.sombraY != 0, "y=" + nv.sombraY);
            ok("hay puerta de Luz", tiene(nv, Constantes.PUERTA_LUZ), "");
            ok("hay puerta de Sombra", tiene(nv, Constantes.PUERTA_SOMBRA), "");
            ok("hay albores y obsidianas", nv.totalAlbores > 0 && nv.totalObsidianas > 0,
               nv.totalAlbores + " / " + nv.totalObsidianas);

            // caer libremente 4 segundos: deben quedar vivos y apoyados
            for (int t = 0; t < 240; t++) n.actualizar();
            ok("Luz sobrevive al arranque", n.luz.vivo, "enSuelo=" + n.luz.enSuelo);
            ok("Sombra sobrevive al arranque", n.sombra.vivo, "enSuelo=" + n.sombra.enSuelo);
            ok("los dos acaban apoyados", n.luz.enSuelo && n.sombra.enSuelo,
               "luz=" + n.luz.enSuelo + " sombra=" + n.sombra.enSuelo);
            ok("no se sale nadie del nivel",
               dentro(n.luz) && dentro(n.sombra),
               "luz=(" + n.luz.x + "," + n.luz.y + ") sombra=(" + n.sombra.x + "," + n.sombra.y + ")");
            ok("no empiezan ganando", !n.hanGanado(), "");

            // cada puerta debe tener suelo justo debajo
            ok("las puertas tienen suelo", puertasConSuelo(nv), "");
        }
        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN" : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
    static boolean tiene(Nivel nv, char t) {
        for (Elemento e : nv.elementos) if (e.tipo == t) return true;
        return false;
    }
    static boolean dentro(Jugador j) {
        return j.x >= 0 && j.y >= 0
            && j.x + j.ancho <= Constantes.COLUMNAS * Constantes.CELDA
            && j.y + j.alto <= Constantes.FILAS * Constantes.CELDA;
    }
    static boolean puertasConSuelo(Nivel nv) {
        for (Elemento e : nv.elementos) {
            if (e.tipo != Constantes.PUERTA_LUZ && e.tipo != Constantes.PUERTA_SOMBRA) continue;
            int fila = e.y / Constantes.CELDA + 1;
            int col = e.x / Constantes.CELDA;
            if (fila >= nv.filas || nv.mapa[fila][col] != Constantes.BLOQUE) {
                System.out.println("      puerta '" + e.tipo + "' sin suelo en fila " + fila + " col " + col);
                return false;
            }
        }
        return true;
    }
}
