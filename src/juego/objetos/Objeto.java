package juego.objetos;

import java.awt.Rectangle;

public class Objeto {

    public int x;
    public int y;
    public int ancho;
    public int alto;

    public Objeto(int x, int y, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    public Rectangle getRectangulo() {
        return new Rectangle(x, y, ancho, alto);
    }

    // Rectangulo mas pequeno, para que rozar un borde no cuente como tocar.
    public Rectangle getRectangulo(int margen) {
        return new Rectangle(x + margen, y + margen, ancho - margen * 2, alto - margen * 2);
    }
}
