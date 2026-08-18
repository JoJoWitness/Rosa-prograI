package juego.utilidades;

import java.awt.Rectangle;

// Boton dibujado a mano. No es un JButton: un componente Swing focusable
// le robaria el teclado al Lienzo.
public class Boton {

    public final Rectangle zona;
    public final String texto;
    public boolean activo;

    public Boton(int x, int y, int ancho, int alto, String texto) {
        this.zona = new Rectangle(x, y, ancho, alto);
        this.texto = texto;
        this.activo = true;
    }

    public boolean contiene(int x, int y) {
        return activo && zona.contains(x, y);
    }
}
