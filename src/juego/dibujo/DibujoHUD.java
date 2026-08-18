package juego.dibujo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import juego.controladores.Estado;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.utilidades.Constantes;

public class DibujoHUD {

    private static final Font FUENTE_HUD = new Font("SansSerif", Font.PLAIN, 26);
    private static final Font FUENTE_TITULO = new Font("SansSerif", Font.PLAIN, 64);
    private static final Font FUENTE_AYUDA = new Font("SansSerif", Font.PLAIN, 30);

    public static void dibujar(Graphics2D g, Nivel nivel, Jugador luz, Jugador sombra, Estado estado) {
        dibujarIconoPausa(g);
        dibujarContadores(g, nivel, luz, sombra);

        if (estado == Estado.GANADO) {
            dibujarCartel(g, "NIVEL COMPLETADO", "R para reiniciar");
        } else if (estado == Estado.PERDIDO) {
            dibujarCartel(g, "FIN DEL JUEGO", "R para reintentar");
        }
    }

    // Diagnostico de la tecla G: que teclas ve el juego y a que ritmo va.
    // Si una tecla no se enciende al pulsarla, el problema es del teclado.
    public static void dibujarDiagnostico(Graphics2D g, String[] nombres, boolean[] pulsadas, double fps) {
        int ancho = 92 * nombres.length + 210;
        int x = Constantes.ANCHO - ancho - Constantes.MARGEN_X - 10;
        int y = Constantes.MARGEN_Y + 12;

        g.setColor(new Color(18, 16, 14, 215));
        g.fillRoundRect(x, y, ancho, 52, 12, 12);

        g.setFont(FUENTE_HUD);
        for (int i = 0; i < nombres.length; i++) {
            int bx = x + 12 + i * 92;
            g.setColor(pulsadas[i] ? new Color(120, 230, 140) : new Color(70, 62, 54));
            g.fillRoundRect(bx, y + 9, 82, 34, 8, 8);
            g.setColor(pulsadas[i] ? new Color(15, 30, 18) : new Color(150, 140, 128));
            int w = g.getFontMetrics().stringWidth(nombres[i]);
            g.drawString(nombres[i], bx + (82 - w) / 2, y + 33);
        }

        g.setColor(fps >= 55 ? new Color(120, 230, 140) : new Color(240, 170, 90));
        g.drawString(String.format("%.0f FPS", fps), x + ancho - 190, y + 33);
    }

    private static void dibujarIconoPausa(Graphics2D g) {
        g.setColor(Constantes.COLOR_PUAS);
        int x = Constantes.MARGEN_X + 24;
        int y = Constantes.MARGEN_Y + 22;
        g.fillRect(x, y, 9, 34);
        g.fillRect(x + 16, y, 9, 34);
    }

    private static void dibujarContadores(Graphics2D g, Nivel nivel, Jugador luz, Jugador sombra) {
        g.setFont(FUENTE_HUD);

        int y = Constantes.MARGEN_Y + 48;

        g.setColor(Constantes.COLOR_LUZ);
        g.fillRect(Constantes.MARGEN_X + 90, y - 22, 22, 26);
        g.drawString(luz.recogidos + " / " + nivel.totalAlbores, Constantes.MARGEN_X + 124, y);

        g.setColor(Constantes.COLOR_SOMBRA);
        g.fillRect(Constantes.MARGEN_X + 240, y - 22, 22, 26);
        g.drawString(sombra.recogidos + " / " + nivel.totalObsidianas, Constantes.MARGEN_X + 274, y);
    }

    private static void dibujarCartel(Graphics2D g, String titulo, String ayuda) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, Constantes.ANCHO, Constantes.ALTO);

        g.setColor(Constantes.COLOR_LUZ);

        g.setFont(FUENTE_TITULO);
        centrar(g, titulo, Constantes.ALTO / 2 - 20);

        g.setFont(FUENTE_AYUDA);
        centrar(g, ayuda, Constantes.ALTO / 2 + 50);
    }

    /**
     * Cartel de que faltan los archivos. Va en pantalla y no solo en la consola
     * porque al abrir el jar con doble clic no hay ninguna consola donde mirar,
     * y lo que se ve si no es un menu en blanco sin explicacion.
     */
    public static void dibujarSinAssets(Graphics2D g) {
        int alto = 170;
        g.setColor(new Color(120, 20, 20, 235));
        g.fillRect(0, 0, Constantes.ANCHO, alto);

        g.setColor(new Color(255, 235, 230));
        g.setFont(FUENTE_AYUDA);
        centrar(g, "NO ENCUENTRO LA CARPETA assets/", 52);

        g.setFont(FUENTE_HUD);
        centrar(g, "La espero en:  " + juego.utilidades.Assets.donde() + "\\assets", 98);
        centrar(g, "Copia ahi la carpeta assets/ entera, "
                   + "o usa el Luz-y-Sombra.jar que ya la lleva dentro", 140);
    }

    private static void centrar(Graphics2D g, String texto, int y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, (Constantes.ANCHO - ancho) / 2, y);
    }

    private DibujoHUD() {
    }
}
