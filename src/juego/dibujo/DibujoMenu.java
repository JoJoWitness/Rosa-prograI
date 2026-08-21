package juego.dibujo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import juego.objetos.Nivel;
import juego.utilidades.Boton;
import juego.utilidades.Constantes;
import juego.utilidades.Recursos;

public class DibujoMenu {

    private static final Color ORO_CLARO = new Color(214, 184, 116);
    private static final Color ORO_OSCURO = new Color(160, 128, 66);
    private static final Color ORO_APAGADO = new Color(120, 100, 66);
    private static final Color BORDE = new Color(70, 58, 44);
    private static final Color TINTA = new Color(38, 32, 26);
    private static final Color PAPEL = new Color(238, 233, 222);
    private static final Color PANEL = new Color(28, 24, 20, 225);

    private static final Font FUENTE_BOTON = new Font("SansSerif", Font.PLAIN, 38);
    private static final Font FUENTE_TITULO = new Font("SansSerif", Font.PLAIN, 52);
    private static final Font FUENTE_TEXTO = new Font("SansSerif", Font.PLAIN, 30);
    private static final Font FUENTE_MARCA = new Font("Serif", Font.BOLD | Font.ITALIC, 120);

    /** Centro horizontal del panel de los menus (110..1810). */
    private static final int CENTRO = 960;

    public static void fondoMenus(Graphics2D g) {
        BufferedImage img = Recursos.fondo("menus");
        if (img != null) {
            g.drawImage(img, 0, 0, Constantes.ANCHO, Constantes.ALTO, null);
        } else {
            g.setColor(new Color(96, 88, 74));
            g.fillRect(0, 0, Constantes.ANCHO, Constantes.ALTO);
        }
    }

    public static void menuPrincipal(Graphics2D g, Boton jugar, Boton tutorial, int mx, int my) {
        // La portada ya trae dibujados los dos personajes y el nombre del
        // juego, asi que aqui no se pinta ninguno de los dos: se hacia cuando
        // el fondo era una textura lisa, y sobre la portada salia todo doble.
        fondoMenus(g);

        boton(g, jugar, mx, my);
        boton(g, tutorial, mx, my);
    }

    public static void seleccionNiveles(Graphics2D g, Boton[] casillas, Nivel[] previos,
                                        int desbloqueados, int mx, int my) {
        fondoMenus(g);
        rotulo(g, "JUGAR", 110, 108, 1700, 82);

        for (int i = 0; i < casillas.length; i++) {
            Boton c = casillas[i];
            boolean abierta = i < desbloqueados;

            if (abierta && previos[i] != null) {
                DibujoNivel.dibujarMiniatura(g, previos[i], c.zona.x, c.zona.y, c.zona.width, c.zona.height);
            } else {
                g.setColor(new Color(58, 48, 40, 220));
                g.fillRoundRect(c.zona.x, c.zona.y, c.zona.width, c.zona.height, 30, 30);
                candado(g, c.zona.x + c.zona.width / 2, c.zona.y + c.zona.height / 2);
            }

            g.setColor(abierta && c.contiene(mx, my) ? ORO_CLARO : BORDE);
            g.drawRoundRect(c.zona.x, c.zona.y, c.zona.width, c.zona.height, 30, 30);
        }
    }

    public static void tutorial(Graphics2D g, int pagina, Boton anterior, Boton siguiente,
                                Boton volver, int mx, int my) {
        fondoMenus(g);
        rotulo(g, "TUTORIAL", 620, 108, 680, 82);

        g.setColor(PANEL);
        g.fillRoundRect(110, 275, 1700, 690, 40, 40);
        g.setColor(BORDE);
        g.drawRoundRect(110, 275, 1700, 690, 40, 40);

        if (pagina == 0) {
            // Cada personaje a la altura de SU bloque: Luz arriba a la
            // izquierda y Sombra abajo a la derecha. Antes los dos estaban
            // arriba y el texto de Sombra caia lejos de ella.
            personaje(g, "luz_quieto", 170, 330, 200, 290);
            personaje(g, "sombra_quieto", 1560, 620, 200, 290);
            parrafoCentrado(g, CENTRO, 420, new String[] {
                "Utiliza las teclas A, W, D para mover a Luz.",
                "Luz no puede atravesar las penumbras.",
                "Ayudala a recolectar el Albor." });
            parrafoCentrado(g, CENTRO, 710, new String[] {
                "Utiliza las flechas para mover a Sombra.",
                "Sombra no puede pasar por la luminosidad.",
                "Ayudalo a recolectar las Obsidianas." });
        } else if (pagina == 1) {
            // Las dos columnas repartidas a los lados del centro del panel:
            // estaban en 640 y 1120, o sea centradas en 880 y no en 960.
            int izq = CENTRO - 240;
            int der = CENTRO + 240;
            elemento(g, "albor", izq, 430, 150);
            elemento(g, "obsidiana", der, 430, 150);
            etiqueta(g, "ALBOR", izq, 640);
            etiqueta(g, "OBSIDIANA", der, 640);
            parrafoCentrado(g, CENTRO, 780, new String[] {
                "El Albor de la manana que ilumina el nuevo mundo.",
                "La Obsidiana de la tierra que forja el nuevo mundo." });
        } else {
            // Tres filas iguales: el dibujo en una caja fija a la izquierda y
            // el texto siempre arrancando en la misma columna.
            int[] filas = { 390, 620, 850 };
            String[] dibujos = { "pozo_luminoso", "pozo_penumbra", "espinas" };
            String[] textos = {
                "La Luminosidad puede extinguir a Sombra.",
                "Las Penumbras pueden apagar a Luz.",
                "Las puas danan a Luz y a Sombra." };

            for (int i = 0; i < filas.length; i++) {
                enCaja(g, dibujos[i], 420, filas[i], 300, 130);
                parrafo(g, 640, filas[i] + 16, new String[] { textos[i] });
            }
        }

        if (pagina > 0) {
            flecha(g, anterior, false, mx, my);
        }
        if (pagina < 2) {
            flecha(g, siguiente, true, mx, my);
        }
        boton(g, volver, mx, my);
    }

    public static void panelSobreNivel(Graphics2D g, String titulo, String ayuda,
                                       Boton[] botones, int mx, int my) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, Constantes.ANCHO, Constantes.ALTO);

        rotulo(g, titulo, 500, 250, 920, 88);

        if (ayuda != null) {
            g.setFont(FUENTE_TEXTO);
            g.setColor(PAPEL);
            centrar(g, ayuda, Constantes.ANCHO / 2, 470);
        }

        for (Boton b : botones) {
            boton(g, b, mx, my);
        }
    }

    // ---------- piezas ----------

    public static void boton(Graphics2D g, Boton b, int mx, int my) {
        int x = b.zona.x;
        int y = b.zona.y;
        int w = b.zona.width;
        int h = b.zona.height;

        Color arriba = b.activo ? (b.contiene(mx, my) ? PAPEL : ORO_CLARO) : ORO_APAGADO;
        g.setPaint(new GradientPaint(x, y, arriba, x, y + h, ORO_OSCURO));
        g.fillRoundRect(x, y, w, h, h, h);

        g.setPaint(BORDE);
        g.drawRoundRect(x, y, w, h, h, h);

        g.setFont(FUENTE_BOTON);
        g.setColor(TINTA);
        espaciado(g, b.texto, x + w / 2, y + h / 2 + 13, 8);
    }

    private static void rotulo(Graphics2D g, String texto, int x, int y, int w, int h) {
        g.setPaint(new GradientPaint(x, y, ORO_CLARO, x, y + h, ORO_OSCURO));
        g.fillRoundRect(x, y, w, h, h, h);
        g.setPaint(BORDE);
        g.drawRoundRect(x, y, w, h, h, h);

        g.setFont(FUENTE_TITULO);
        g.setColor(TINTA);
        espaciado(g, texto, x + w / 2, y + h / 2 + 18, 12);
    }

    private static void flecha(Graphics2D g, Boton b, boolean haciaDerecha, int mx, int my) {
        int x = b.zona.x;
        int y = b.zona.y;
        int w = b.zona.width;
        int h = b.zona.height;

        int[] px;
        int[] py = new int[] { y + h / 4, y + h / 4, y, y + h / 2, y + h, y + h * 3 / 4, y + h * 3 / 4 };

        if (haciaDerecha) {
            px = new int[] { x, x + w / 2, x + w / 2, x + w, x + w / 2, x + w / 2, x };
        } else {
            px = new int[] { x + w, x + w / 2, x + w / 2, x, x + w / 2, x + w / 2, x + w };
        }

        g.setColor(b.contiene(mx, my) ? PAPEL : new Color(226, 220, 205));
        g.fillPolygon(px, py, px.length);
    }

    private static void candado(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(12, 10, 9));
        g.fillRoundRect(cx - 55, cy - 15, 110, 95, 14, 14);
        g.fillOval(cx - 42, cy - 78, 84, 84);
        g.setColor(new Color(58, 48, 40, 220));
        g.fillOval(cx - 22, cy - 58, 44, 50);
        g.setColor(new Color(58, 48, 40, 220));
        g.fillOval(cx - 13, cy + 12, 26, 26);
        g.fillRect(cx - 6, cy + 28, 12, 30);
    }

    private static void personaje(Graphics2D g, String nombre, int x, int y, int w, int h) {
        BufferedImage img = Recursos.sprite(nombre);
        if (img != null) {
            g.drawImage(img, x, y, w, h, null);
        }
    }

    private static void elemento(Graphics2D g, String nombre, int cx, int cy, int alto) {
        BufferedImage img = Recursos.spriteRecortado(nombre);
        if (img == null) {
            return;
        }
        int ancho = alto * img.getWidth() / img.getHeight();
        g.drawImage(img, cx - ancho / 2, cy - alto / 2, ancho, alto, null);
    }

    /**
     * Encaja el dibujo dentro de la caja sin deformarlo: manda la medida que
     * primero se queda sin sitio.
     *
     * Hace falta porque {@link #elemento} escala siempre por el alto, y los
     * charcos son sprites muy anchos y bajos: la Luminosidad mide 236x45, o sea
     * 5,24 de proporcion, asi que pedirle 120 de alto le daba 629 px de ancho y
     * se comia el texto de al lado.
     */
    private static void enCaja(Graphics2D g, String nombre, int cx, int cy,
                               int maxAncho, int maxAlto) {
        BufferedImage img = Recursos.spriteRecortado(nombre);
        if (img == null) {
            return;
        }

        double escala = Math.min(maxAncho / (double) img.getWidth(),
                                 maxAlto / (double) img.getHeight());
        int ancho = (int) Math.round(img.getWidth() * escala);
        int alto = (int) Math.round(img.getHeight() * escala);
        g.drawImage(img, cx - ancho / 2, cy - alto / 2, ancho, alto, null);
    }

    private static void etiqueta(Graphics2D g, String texto, int cx, int y) {
        g.setFont(FUENTE_TEXTO);
        g.setColor(PAPEL);
        espaciado(g, texto, cx, y, 6);
    }

    /** Igual que parrafo, pero cada linea centrada en esa columna. */
    private static void parrafoCentrado(Graphics2D g, int cx, int y, String[] lineas) {
        g.setFont(FUENTE_TEXTO);
        g.setColor(PAPEL);
        for (int i = 0; i < lineas.length; i++) {
            int ancho = g.getFontMetrics().stringWidth(lineas[i]);
            g.drawString(lineas[i], cx - ancho / 2, y + i * 46);
        }
    }

    private static void parrafo(Graphics2D g, int x, int y, String[] lineas) {
        g.setFont(FUENTE_TEXTO);
        g.setColor(PAPEL);
        for (int i = 0; i < lineas.length; i++) {
            g.drawString(lineas[i], x, y + i * 46);
        }
    }

    private static void centrar(Graphics2D g, String texto, int cx, int y) {
        g.drawString(texto, cx - g.getFontMetrics().stringWidth(texto) / 2, y);
    }

    // La maqueta usa las mayusculas muy separadas.
    private static void espaciado(Graphics2D g, String texto, int cx, int y, int extra) {
        int total = 0;
        for (int i = 0; i < texto.length(); i++) {
            total += g.getFontMetrics().charWidth(texto.charAt(i)) + extra;
        }

        int x = cx - total / 2;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            g.drawString(String.valueOf(c), x, y);
            x += g.getFontMetrics().charWidth(c) + extra;
        }
    }

    private DibujoMenu() {
    }
}
