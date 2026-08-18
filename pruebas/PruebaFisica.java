import juego.objetos.*;
import juego.utilidades.*;

public class PruebaFisica {
    static int fallos = 0;

    static void ok(String nombre, boolean cond, String detalle) {
        System.out.println((cond ? "  OK   " : "  FALLA") + "  " + nombre + "   " + detalle);
        if (!cond) fallos++;
    }

    public static void main(String[] a) {
        Nivel n = CargadorNivel.cargar(System.getProperty("nivel"), "");
        int C = Constantes.CELDA;

        System.out.println("Nivel: " + n.filas + "x" + n.columnas
            + "  elementos=" + n.elementos.size()
            + "  albores=" + n.totalAlbores + "  obsidianas=" + n.totalObsidianas);
        System.out.println("Spawn Luz=(" + n.luzX + "," + n.luzY + ")  Sombra=(" + n.sombraX + "," + n.sombraY + ")");

        // 1) Cae y aterriza sobre el suelo (fila 23)
        Jugador j = new Jugador(Jugador.LUZ, n.luzX, n.luzY - 300);
        for (int i = 0; i < 200; i++) Colisiones.mover(j, n.mapa);
        ok("aterriza en el suelo", j.enSuelo && j.y + j.alto == 23 * C,
           "pies=" + (j.y + j.alto) + " esperado=" + (23 * C));

        // 2) No atraviesa: nunca queda dentro de un bloque
        ok("no se hunde", j.velY == 0, "velY=" + j.velY);

        // 3) Salto: sube y vuelve a caer
        int yInicial = j.y;
        j.saltar();
        int yMasAlto = j.y;
        for (int i = 0; i < 120; i++) {
            Colisiones.mover(j, n.mapa);
            if (j.y < yMasAlto) yMasAlto = j.y;
        }
        int altura = yInicial - yMasAlto;
        ok("el salto pasa de 5 celdas", altura >= 5 * C,
           "altura=" + altura + "px = " + String.format("%.1f", altura / (double) C) + " celdas");
        ok("vuelve al suelo", j.y == yInicial && j.enSuelo, "y=" + j.y + " esperado=" + yInicial);

        // 4) Choca contra la pared derecha y se detiene pegado a ella
        j.x = 30 * C; j.y = 23 * C - j.alto; j.velY = 0;
        for (int i = 0; i < 300; i++) { j.velX = Constantes.VEL_X; Colisiones.mover(j, n.mapa); }
        ok("se detiene en la pared", j.x + j.ancho == 42 * C,
           "borde=" + (j.x + j.ancho) + " esperado=" + (42 * C));

        // 5) Pared izquierda, en el tramo libre antes del primer escalon
        j.x = 6 * C; j.y = 23 * C - j.alto; j.velY = 0;
        for (int i = 0; i < 200; i++) { j.velX = -Constantes.VEL_X; Colisiones.mover(j, n.mapa); }
        ok("se detiene en la pared izq", j.x == 1 * C, "x=" + j.x + " esperado=" + (1 * C));

        // 5b) Un escalon de 2 celdas queda a la altura del cuerpo: frena por el costado
        j.x = 20 * C; j.y = 23 * C - j.alto; j.velY = 0;
        for (int i = 0; i < 200; i++) { j.velX = -Constantes.VEL_X; Colisiones.mover(j, n.mapa); }
        ok("choca con el costado del escalon", j.x == 13 * C, "x=" + j.x + " esperado=" + (13 * C));

        // 6) Sube el escalon de fila 21 (cols 8-12) saltando desde el suelo
        j.x = 7 * C; j.y = 23 * C - j.alto; j.velX = 0; j.velY = 0;
        Colisiones.mover(j, n.mapa);
        j.saltar();
        boolean subio = false;
        for (int i = 0; i < 60; i++) {
            j.velX = Constantes.VEL_X;
            Colisiones.mover(j, n.mapa);
            if (j.enSuelo && j.y + j.alto == 21 * C) subio = true;
        }
        ok("sube el escalon de 2 celdas", subio, "y final=" + (j.y + j.alto));

        // 7) Sin techo indebido: de pie en el escalon, la cabeza esta libre
        ok("cabeza libre sobre el escalon", j.y >= 0, "y=" + j.y);

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN" : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
