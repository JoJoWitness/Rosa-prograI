import juego.controladores.ControladorNivel;
import juego.objetos.*;
import juego.utilidades.*;

public class PruebaSalto {
    public static void main(String[] a) {
        int C = Constantes.CELDA, COL = 20;
        int v = -Constantes.FUERZA_SALTO, y = 0, vy = -v;
        while (vy < 0) { vy += Constantes.GRAVEDAD; y += vy; }
        System.out.printf("salto libre: %d px = %.2f celdas%n", -y, -y / (double) C);

        int maximo = 0;
        for (int n = 1; n <= 7; n++) {
            boolean subio = false;
            // se prueban varios puntos de despegue, como haria un jugador
            for (int antelacion = 1; antelacion <= 5 && !subio; antelacion++) {
                ControladorNivel cn = new ControladorNivel(
                    CargadorNivel.cargar(a[0] + "/escalon" + n + ".txt", ""));
                Jugador j = cn.luz;
                j.x = 5 * C; j.y = 23 * C - j.alto; j.velY = 0;
                cn.actualizar();
                boolean saltado = false;
                for (int i = 0; i < 160; i++) {
                    j.velX = (j.x < (COL + 2) * C) ? Constantes.VEL_X : 0;
                    if (!saltado && j.x + j.ancho >= (COL - antelacion) * C) {
                        j.saltar(); saltado = true;
                    }
                    cn.actualizar();
                    if (j.enSuelo && j.y + j.alto == (23 - n) * C) { subio = true; break; }
                }
            }
            System.out.printf("  escalon de %d celdas (%3d px): %s%n", n, n * C, subio ? "SUBE" : "no llega");
            if (subio) maximo = n;
        }
        System.out.println("escalon maximo que supera: " + maximo + " celdas");
    }
}
