package juego.objetos;

import juego.utilidades.Constantes;

public class Plataforma extends Objeto {

    public final int yReposo;
    public final int yActiva;
    public final boolean baja;     // baja con peso, o es el contrapeso que sube
    public final int grupo;

    // Cuanto se movio en el ultimo frame, para arrastrar a quien va encima.
    public int deltaY;

    // La celda del simbolo es donde cuelga en reposo. Con peso encima, 'E' baja;
    // 'e' es su contrapeso y sube. La pareja hace el efecto de polea.
    public Plataforma(int fila, int columna, int celdas, boolean baja, int grupo) {
        super(columna * Constantes.CELDA, fila * Constantes.CELDA,
              celdas * Constantes.CELDA, Constantes.ALTO_PLATAFORMA);

        this.baja = baja;
        this.grupo = grupo;
        this.yReposo = fila * Constantes.CELDA;
        this.yActiva = baja
            ? yReposo + Constantes.RECORRIDO_PLATAFORMA
            : yReposo - Constantes.RECORRIDO_PLATAFORMA;
    }

    public void mover(boolean activada) {
        int destino = activada ? yActiva : yReposo;
        int antes = y;

        if (y < destino) {
            y = Math.min(destino, y + Constantes.VEL_PLATAFORMA);
        } else if (y > destino) {
            y = Math.max(destino, y - Constantes.VEL_PLATAFORMA);
        }

        deltaY = y - antes;
    }
}
