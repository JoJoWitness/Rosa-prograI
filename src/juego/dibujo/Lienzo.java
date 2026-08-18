package juego.dibujo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JPanel;

import juego.controladores.JuegoControlador;
import juego.utilidades.Assets;
import juego.utilidades.Constantes;

public class Lienzo extends JPanel {

    /** Sitio que se le deja al borde y a la barra de titulo de la ventana. */
    private static final int HUECO_ANCHO = 24;
    private static final int HUECO_ALTO = 80;

    /** Por debajo de esto la letra de los menus ya no se lee. */
    private static final double ESCALA_MINIMA = 0.5;

    private final JuegoControlador juego;

    /**
     * Cuanto se encoge el juego para caber en la pantalla; 1 es el tamano de
     * verdad. Todo el juego esta dibujado sobre 1920x1080, asi que en un
     * monitor de 1920x1080 la ventana no cabe entera: le estorban la barra de
     * titulo y la del sistema. En vez de recolocarlo todo, se dibuja igual que
     * siempre y se encoge al final.
     *
     * Es el tamano con el que se abre la ventana; a partir de ahi manda
     * escalaVista(), que mira el tamano que tiene el panel de verdad.
     */
    private final double escala;

    public Lienzo(JuegoControlador juego) {
        this.juego = juego;
        this.escala = escalaQueCabe();
        setPreferredSize(new Dimension(anchoVentana(), altoVentana()));
        setBackground(Constantes.COLOR_MARCO);
        setFocusable(true);
    }

    /**
     * Cuanto se encoge el juego, ahora mismo.
     *
     * Se coge el lado que peor va, no uno para el ancho y otro para el alto:
     * asi la imagen conserva su proporcion 16:9 y no se deforma, pase lo que
     * pase con el tamano de la ventana. Si sobrara sitio por un lado, queda de
     * color de marco (margenX() y margenY()), que es preferible a estirar.
     */
    public double escalaVista() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return escala;                       // aun sin dibujar: no hay panel que medir
        }
        return Math.min(getWidth() / (double) Constantes.ANCHO,
                        getHeight() / (double) Constantes.ALTO);
    }

    /** Sitio que sobra a cada lado, para dejar el juego centrado. */
    public int margenX() {
        return (int) Math.round((getWidth() - Constantes.ANCHO * escalaVista()) / 2);
    }

    public int margenY() {
        return (int) Math.round((getHeight() - Constantes.ALTO * escalaVista()) / 2);
    }

    public int anchoVentana() {
        return (int) Math.round(Constantes.ANCHO * escala);
    }

    public int altoVentana() {
        return (int) Math.round(Constantes.ALTO * escala);
    }

    /**
     * Pinta ya, sin pasar por la cola de eventos. Con repaint() el dibujo se
     * encola detras de los eventos de teclado y se suman frames de retraso,
     * que es lo que se siente como teclas pegajosas.
     */
    public void pintarYa() {
        paintImmediately(0, 0, getWidth(), getHeight());
        Toolkit.getDefaultToolkit().sync();
    }

    // Solo lee estado y dibuja: nada de logica aqui.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // El unico sitio donde se tiene en cuenta que la ventana es mas pequena:
        // a partir de aqui todo el juego dibuja sobre 1920x1080 como siempre.
        //
        // Se encoge asi, con la transformacion, y no dibujando primero en una
        // imagen aparte para encogerla despues: esa imagen aparte la tiene que
        // pintar el procesador y sale a 39 ms por fotograma, mientras que asi lo
        // hace la tarjeta grafica y no llega a 0,1 ms.
        //
        // El mismo numero para el ancho y para el alto: es lo que impide que la
        // imagen se deforme.
        double e = escalaVista();
        g2.translate(margenX(), margenY());
        g2.scale(e, e);

        juego.pintar(g2);

        // Encima de todo: si faltan los archivos, ni el menu ni los niveles se
        // ven, y conviene decir por que.
        if (!Assets.hay()) {
            DibujoHUD.dibujarSinAssets(g2);
        }
    }

    /** El mayor tamano que cabe en la pantalla, sin pasar del tamano de verdad. */
    private static double escalaQueCabe() {
        try {
            // Lo que queda de pantalla quitando la barra de tareas.
            Rectangle util = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getMaximumWindowBounds();
            double cabeAncho = (util.width - HUECO_ANCHO) / (double) Constantes.ANCHO;
            double cabeAlto = (util.height - HUECO_ALTO) / (double) Constantes.ALTO;
            double e = Math.min(1.0, Math.min(cabeAncho, cabeAlto));
            return Math.max(ESCALA_MINIMA, e);
        } catch (Exception ex) {
            // Sin pantalla que consultar: se deja el tamano de verdad.
            return 1.0;
        }
    }
}
