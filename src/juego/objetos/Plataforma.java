package juego.objetos;

import juego.utilidades.Constantes;

public class Plataforma extends Objeto {

    /** Punto del que cuelga: el centro del tablon. No se mueve nunca. */
    public final int pivoteX;
    public final int pivoteY;

    /**
     * Hacia donde cae cuando el peso queda justo en el pivote y no desempata.
     * Lo fija el simbolo del .txt: 'E' a la derecha, 'F' a la izquierda.
     */
    public final int sentidoBase;

    /** Inclinacion actual, en radianes. 0 es horizontal. */
    public double angulo;

    // La celda del simbolo es donde cuelga. Cuelga de su centro, asi que el
    // peso no la puede bajar: solo inclinarla. Cada tablon va por su cuenta.
    public Plataforma(int fila, int columna, int celdas, int sentidoBase) {
        super(columna * Constantes.CELDA, fila * Constantes.CELDA,
              celdas * Constantes.CELDA, Constantes.ALTO_PLATAFORMA);

        this.sentidoBase = sentidoBase;
        this.pivoteX = x + ancho / 2;
        this.pivoteY = y;
    }

    /** Altura de la superficie del tablon en esa columna de pixeles. */
    public int alturaEn(int px) {
        return pivoteY + (int) Math.round((px - pivoteX) * Math.tan(angulo));
    }

    /** false cuando esta demasiado volcada: entonces no sostiene a nadie. */
    public boolean sujeta() {
        return Math.abs(angulo) < Constantes.ANG_CAIDA;
    }

    /**
     * El tablon cae hacia el lado por el que le tira el peso: {@code lado} es
     * +1 si esta a la derecha del pivote, -1 si a la izquierda y 0 si no hay
     * nadie encima, y entonces vuelve sola a la horizontal.
     *
     * Lo que NO hace es equilibrarse: la inclinacion no se queda a medias segun
     * lo lejos que este el peso, sigue cayendo hasta el tope. Ponerse en el
     * lado contrario no es apoyarse, es empezar a volcar hacia alli.
     */
    public void girar(int lado) {
        double destino = Constantes.ANG_MAX * lado;

        if (angulo < destino) {
            angulo = Math.min(destino, angulo + Constantes.VEL_GIRO);
        } else if (angulo > destino) {
            angulo = Math.max(destino, angulo - Constantes.VEL_GIRO);
        }
    }
}
