package juego.dibujo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JPanel;

import juego.controladores.JuegoControlador;
import juego.utilidades.Assets;
import juego.utilidades.Constantes;

public class Lienzo extends JPanel {

    private final JuegoControlador juego;

    public Lienzo(JuegoControlador juego) {
        this.juego = juego;
        setPreferredSize(new Dimension(Constantes.ANCHO, Constantes.ALTO));
        setBackground(Constantes.COLOR_MARCO);
        setFocusable(true);
    }

    /**
     * Pinta ya, sin pasar por la cola de eventos. Con repaint() el dibujo se
     * encola detras de los eventos de teclado y se suman frames de retraso,
     * que es lo que se siente como teclas pegajosas.
     */
    public void pintarYa() {
        paintImmediately(0, 0, Constantes.ANCHO, Constantes.ALTO);
        Toolkit.getDefaultToolkit().sync();
    }

    // Solo lee estado y dibuja: nada de logica aqui.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        juego.pintar(g2);

        // Encima de todo: si faltan los archivos, ni el menu ni los niveles se
        // ven, y conviene decir por que.
        if (!Assets.hay()) {
            DibujoHUD.dibujarSinAssets(g2);
        }
    }
}
