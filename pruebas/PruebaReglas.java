import juego.controladores.ControladorNivel;
import juego.objetos.*;
import juego.utilidades.*;

public class PruebaReglas {
    static int fallos = 0;
    static final int C = Constantes.CELDA;

    static void ok(String nombre, boolean cond, String detalle) {
        System.out.println((cond ? "  OK   " : "  FALLA") + "  " + nombre + "   " + detalle);
        if (!cond) fallos++;
    }

    static ControladorNivel nuevo() {
        return new ControladorNivel(CargadorNivel.cargar(System.getProperty("nivel"), ""));
    }

    // Coloca al jugador de pie en el suelo (fila 23), centrado en una columna
    static void enSuelo(Jugador j, int col) {
        j.x = col * C + (C - j.ancho) / 2;
        j.y = 23 * C - j.alto;
        j.velX = 0; j.velY = 0;
    }

    static void enPuerta(Jugador j, int col) {
        j.x = col * C + (C - j.ancho) / 2;
        j.y = 13 * C - j.alto;
        j.velX = 0; j.velY = 0;
    }

    public static void main(String[] a) {
        ControladorNivel n = nuevo();
        System.out.println("albores=" + n.nivel.totalAlbores + "  obsidianas=" + n.nivel.totalObsidianas
            + "  elementos=" + n.nivel.elementos.size());

        // --- PUAS (col 26): mueren los dos ---
        n = nuevo(); enSuelo(n.luz, 26); enSuelo(n.sombra, 6); n.actualizar();
        ok("puas matan a Luz", !n.luz.vivo, "vivo=" + n.luz.vivo);
        n = nuevo(); enSuelo(n.sombra, 26); enSuelo(n.luz, 3); n.actualizar();
        ok("puas matan a Sombra", !n.sombra.vivo, "vivo=" + n.sombra.vivo);

        // --- LUMINOSIDAD (col 31): mata a Sombra, Luz pasa ---
        n = nuevo(); enSuelo(n.sombra, 31); enSuelo(n.luz, 3); n.actualizar();
        ok("Luminosidad extingue a Sombra", !n.sombra.vivo, "vivo=" + n.sombra.vivo);
        n = nuevo(); enSuelo(n.luz, 31); enSuelo(n.sombra, 6); n.actualizar();
        ok("Luz atraviesa la Luminosidad", n.luz.vivo, "vivo=" + n.luz.vivo);

        // --- PENUMBRA (col 36): mata a Luz, Sombra pasa ---
        n = nuevo(); enSuelo(n.luz, 36); enSuelo(n.sombra, 6); n.actualizar();
        ok("Penumbra apaga a Luz", !n.luz.vivo, "vivo=" + n.luz.vivo);
        n = nuevo(); enSuelo(n.sombra, 36); enSuelo(n.luz, 3); n.actualizar();
        ok("Sombra atraviesa la Penumbra", n.sombra.vivo, "vivo=" + n.sombra.vivo);

        // --- Coleccionables: albor 'o' en (20,10), obsidiana 'x' en (18,17) ---
        n = nuevo();
        n.luz.x = 10 * C + 4; n.luz.y = 21 * C - n.luz.alto;
        n.actualizar();
        ok("Luz recoge el albor", n.luz.recogidos == 1, "recogidos=" + n.luz.recogidos);

        n = nuevo();
        n.sombra.x = 10 * C + 4; n.sombra.y = 21 * C - n.sombra.alto;
        n.actualizar();
        ok("Sombra no recoge albores", n.sombra.recogidos == 0, "recogidos=" + n.sombra.recogidos);

        n = nuevo();
        n.sombra.x = 17 * C + 4; n.sombra.y = 19 * C - n.sombra.alto;
        n.actualizar();
        ok("Sombra recoge la obsidiana", n.sombra.recogidos == 1, "recogidos=" + n.sombra.recogidos);

        n = nuevo();
        n.luz.x = 17 * C + 4; n.luz.y = 19 * C - n.luz.alto;
        n.actualizar();
        ok("Luz no recoge obsidianas", n.luz.recogidos == 0, "recogidos=" + n.luz.recogidos);

        // No se recoge dos veces
        n = nuevo();
        n.luz.x = 10 * C + 4; n.luz.y = 21 * C - n.luz.alto;
        for (int i = 0; i < 30; i++) n.actualizar();
        ok("el albor solo cuenta una vez", n.luz.recogidos == 1, "recogidos=" + n.luz.recogidos);

        // --- Victoria: puertas P en col 37, Q en col 39 (fila 12) ---
        n = nuevo(); enPuerta(n.luz, 37); enSuelo(n.sombra, 6); n.actualizar();
        ok("no gana con uno solo en su puerta", !n.hanGanado(), "ganado=" + n.hanGanado());

        n = nuevo(); enPuerta(n.luz, 37); enPuerta(n.sombra, 39); n.actualizar();
        ok("gana con los dos en su puerta", n.hanGanado(), "ganado=" + n.hanGanado());

        n = nuevo(); enPuerta(n.luz, 39); enPuerta(n.sombra, 37); n.actualizar();
        ok("no gana con las puertas cambiadas", !n.hanGanado(), "ganado=" + n.hanGanado());

        // Salirse de la puerta deja de contar
        n = nuevo(); enPuerta(n.luz, 37); enPuerta(n.sombra, 39); n.actualizar();
        boolean antes = n.hanGanado();
        for (int i = 0; i < 20; i++) { n.sombra.velX = Constantes.VEL_X; n.actualizar(); }
        ok("salirse de la puerta cancela", antes && !n.hanGanado(), "antes=" + antes + " despues=" + n.hanGanado());

        // --- hayMuerto ---
        n = nuevo(); enSuelo(n.luz, 26); n.actualizar();
        ok("hayMuerto detecta la muerte", n.hayMuerto(), "muerto=" + n.hayMuerto());

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN" : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
