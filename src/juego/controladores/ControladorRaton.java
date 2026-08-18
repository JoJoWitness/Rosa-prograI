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

    @Override
    public void mousePressed(MouseEvent e) {
        clicX = e.getX();
        clicY = e.getY();
        hayClic = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ratonX = e.getX();
        ratonY = e.getY();
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
