package juego.objetos;

import juego.utilidades.Constantes;

public class Jugador extends Objeto {

    public static final int LUZ = 0;
    public static final int SOMBRA = 1;

    public int tipo;
    public int velX;
    public int velY;
    public boolean enSuelo;
    public boolean vivo;
    public boolean mirandoDerecha;
    public int recogidos;

    public int fotograma;
    public int contadorAnim;

    public Jugador(int tipo, int x, int y) {
        super(x, y, Constantes.ANCHO_JUGADOR, Constantes.ALTO_JUGADOR);
        this.tipo = tipo;
        this.vivo = true;
        this.mirandoDerecha = true;
    }

    public void saltar() {
        if (enSuelo) {
            velY = Constantes.FUERZA_SALTO;
            enSuelo = false;
        }
    }
}
