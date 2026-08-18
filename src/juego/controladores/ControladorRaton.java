package juego.controladores;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import juego.utilidades.Boton;

public class ControladorRaton extends MouseAdapter {

    private int clicX;
    private int clicY;
    private boolean hayClic;

    public int ratonX;
    public int ratonY;

    // La ventana se dibuja encogida para caber en la pantalla, pero los botones
    // estan colocados sobre los 1920x1080 de siempre. Aqui se deshace ese
    // encogimiento, para que el clic caiga donde el jugador lo esta viendo.
    private double escala = 1;
    private int margenX;
    private int margenY;

    /** Los mismos numeros con los que el Lienzo encoge y centra el dibujo. */
    public void ajustarVista(double escala, int margenX, int margenY) {
        this.escala = escala > 0 ? escala : 1;
        this.margenX = margenX;
        this.margenY = margenY;
    }

    private int aJuegoX(int x) {
        return (int) Math.round((x - margenX) / escala);
    }

    private int aJuegoY(int y) {
        return (int) Math.round((y - margenY) / escala);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        clicX = aJuegoX(e.getX());
        clicY = aJuegoY(e.getY());
        hayClic = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ratonX = aJuegoX(e.getX());
        ratonY = aJuegoY(e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public boolean clicEn(Boton b) {
        return hayClic && b.contiene(clicX, clicY);
    }

    public boolean encima(Boton b) {
        return b.contiene(ratonX, ratonY);
    }

    // Se llama una vez por tick, al final: si no, un mismo clic
    // dispararia varias pantallas seguidas.
    public void consumir() {
        hayClic = false;
    }
}
