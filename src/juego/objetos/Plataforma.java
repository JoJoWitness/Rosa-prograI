package juego.objetos;

import juego.utilidades.Constantes;

public class Plataforma extends Objeto {

    /** Punto del que cuelga: el centro del tablon. No se mueve nunca. */
    public final int pivoteX;
    public final int pivoteY;

    /**
     * El lado del simbolo. En un tablon ('E' / 'F') es hacia donde cae cuando
     * el peso queda justo en el pivote y no desempata. En una polea ('e' / 'f')
     * es que extremo de la cuerda es: +1 el que baja con peso, -1 el que sube.
     */
    public final int sentidoBase;

    /** true si cuelga de una polea. Entonces no gira: sube y baja. */
    public final boolean polea;

    /** El otro extremo de la misma cuerda. Lo ata el cargador; null si no hay. */
    public Plataforma pareja;

    /** Lo que lleva recorrido desde su sitio de reposo, en pixeles hacia abajo. */
    public int desplazamiento;

    /** Inclinacion actual, en radianes. 0 es horizontal. Solo la usa el tablon. */
    public double angulo;

    // La celda del simbolo es donde cuelga. El tablon cuelga de su centro, asi
    // que el peso no lo puede bajar: solo inclinarlo, y cada uno va por su
    // cuenta. La polea es lo contrario: no se inclina, se traslada, y no va por
    // su cuenta porque comparte cuerda con el otro extremo.
    public Plataforma(char simbolo, int fila, int columna, int celdas) {
        super(columna * Constantes.CELDA, fila * Constantes.CELDA,
              celdas * Constantes.CELDA, Constantes.ALTO_PLATAFORMA);

        this.sentidoBase = Constantes.sentidoDe(simbolo);
        this.polea = Constantes.esPolea(simbolo);
        this.pivoteX = x + ancho / 2;
        this.pivoteY = y;
    }

    /** Altura de la superficie del tablon en esa columna de pixeles. */
    public int alturaEn(int px) {
        return pivoteY + desplazamiento
             + (int) Math.round((px - pivoteX) * Math.tan(angulo));
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

    /**
     * Tira de la cuerda hasta dejar este extremo a {@code destino} pixeles por
     * debajo de su sitio de reposo. La cuerda no da de si: **lo que baja este
     * extremo lo sube el otro, y siempre lo mismo**, asi que la pareja se
     * queda justo al reves. Es la unica regla de la polea; no hay pesos que
     * sumar ni inercia que acumular.
     */
    public void deslizar(int destino) {
        if (desplazamiento < destino) {
            desplazamiento = Math.min(destino, desplazamiento + Constantes.VEL_POLEA);
        } else if (desplazamiento > destino) {
            desplazamiento = Math.max(destino, desplazamiento - Constantes.VEL_POLEA);
        }

        if (pareja != null) {
            pareja.desplazamiento = -desplazamiento;
        }
    }
}
