package juego.objetos;

import juego.utilidades.Constantes;

public class Elemento extends Objeto {

    public char tipo;
    public boolean activo;

    // Solo para las palancas: una vez echada se queda echada.
    public boolean encendida;

    /** Grupo del mecanismo; -1 si el elemento no es un interruptor. */
    public int grupo;

    public Elemento(char tipo, int fila, int columna) {
        this(tipo, fila, columna, 1);
    }

    // Las celdas seguidas del mismo tipo se juntan en un solo elemento: asi un
    // charco de 3 celdas se dibuja como una mancha y no como tres repetidas.
    public Elemento(char tipo, int fila, int columna, int celdas) {
        super(columna * Constantes.CELDA, fila * Constantes.CELDA,
              celdas * Constantes.CELDA, Constantes.CELDA);
        this.tipo = tipo;
        this.activo = true;
        this.grupo = Constantes.grupoDe(tipo);
    }
}
