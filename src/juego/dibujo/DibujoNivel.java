package juego.dibujo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import juego.objetos.Elemento;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.objetos.Plataforma;
import juego.utilidades.Constantes;
import juego.utilidades.Recursos;

public class DibujoNivel {

    private static final Stroke SOLIDO = new BasicStroke(1);
    private static final Font FUENTE_REJILLA = new Font("SansSerif", Font.PLAIN, 15);
    private static final Stroke CUERDA =
        new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] {7, 7}, 0);

    // El lienzo de los personajes mide 420x594. Se dibuja entero y no recortado
    // para que los fotogramas no salten entre si. Las medidas salen de la altura
    // del jugador, para que cambiarla no descuadre el sprite.
    // Medidos sobre la maquetacion: los cristales ocupan 1,8 celdas de alto y
    // las puertas 4. Con menos se ven diminutos al lado de los personajes.
    private static final int ALTO_CRISTAL = Constantes.CELDA * 18 / 10;
    private static final int ALTO_PUERTA = Constantes.CELDA * 4;
    // El dibujo de las espinas cubre 3 celdas. Estirado a mas se convierte en
    // una sola pua enorme, asi que una hilera larga se pinta a trozos: 4 celdas
    // salen como un grupo de 3 y otro de 1.
    private static final int ALTO_ESPINAS = 34;
    private static final int ALTO_PALANCA = 40;
    private static final int CELDAS_ESPINAS = 3;
    // La cuerda de la polea cruza el techo por la fila 4,5 y el nudo queda 3
    // celdas por encima de la losa. Las dos medidas salen de 'docs/3.png'.
    private static final int ALTO_CUERDA = Constantes.CELDA * 9 / 2;
    private static final int ALTO_NUDO = Constantes.CELDA * 3;

    private static final int SPRITE_ALTO = Constantes.ALTO_JUGADOR * 126 / 100;
    private static final int SPRITE_ANCHO = SPRITE_ALTO * 420 / 594;
    private static final int SPRITE_BASE = SPRITE_ALTO * 94 / 100;   // del borde superior a los pies
    private static final int SPRITE_CENTRO = SPRITE_ANCHO / 2;       // del borde izquierdo al eje

    public static void dibujar(Graphics2D g, Nivel nivel, Jugador luz, Jugador sombra) {
        dibujarFondo(g, nivel);
        dibujarBloques(g, nivel);
        dibujarElementos(g, nivel);
        dibujarMuros(g, nivel);
        dibujarPlataformas(g, nivel);
        dibujarJugador(g, luz);
        dibujarJugador(g, sombra);
    }

    // Rejilla de depuracion: se activa con G para comprobar a ojo que las celdas
    // del mapa caen sobre el marco que traen pintado los fondos.
    public static void dibujarRejilla(Graphics2D g) {
        int ancho = Constantes.COLUMNAS * Constantes.CELDA;
        int alto = Constantes.FILAS * Constantes.CELDA;

        g.setStroke(SOLIDO);
        g.setColor(new Color(255, 80, 80, 90));
        for (int c = 0; c <= Constantes.COLUMNAS; c++) {
            int x = Constantes.MARGEN_X + c * Constantes.CELDA;
            g.drawLine(x, Constantes.MARGEN_Y, x, Constantes.MARGEN_Y + alto);
        }
        for (int f = 0; f <= Constantes.FILAS; f++) {
            int y = Constantes.MARGEN_Y + f * Constantes.CELDA;
            g.drawLine(Constantes.MARGEN_X, y, Constantes.MARGEN_X + ancho, y);
        }

        // borde exterior del mapa y borde del area interior (la fila/columna de muro)
        g.setColor(new Color(255, 60, 60, 230));
        g.drawRect(Constantes.MARGEN_X, Constantes.MARGEN_Y, ancho, alto);
        g.setColor(new Color(80, 210, 255, 230));
        g.drawRect(Constantes.MARGEN_X + Constantes.CELDA,
                   Constantes.MARGEN_Y + Constantes.CELDA,
                   ancho - 2 * Constantes.CELDA, alto - 2 * Constantes.CELDA);

        g.setFont(FUENTE_REJILLA);
        g.setColor(new Color(255, 230, 230, 220));
        for (int c = 0; c <= Constantes.COLUMNAS; c += 5) {
            g.drawString(String.valueOf(c),
                Constantes.MARGEN_X + c * Constantes.CELDA + 3, Constantes.MARGEN_Y + 14);
        }
        for (int f = 0; f <= Constantes.FILAS; f += 5) {
            g.drawString(String.valueOf(f),
                Constantes.MARGEN_X + 3, Constantes.MARGEN_Y + f * Constantes.CELDA + 14);
        }

        g.setColor(new Color(20, 18, 16, 200));
        g.fillRect(Constantes.MARGEN_X + ancho - 470, Constantes.MARGEN_Y + alto - 40, 470, 40);
        g.setColor(new Color(255, 235, 220));
        g.drawString("celda " + Constantes.CELDA + " px   rejilla "
                     + Constantes.COLUMNAS + " x " + Constantes.FILAS
                     + "   area " + ancho + " x " + alto,
                     Constantes.MARGEN_X + ancho - 458, Constantes.MARGEN_Y + alto - 14);
    }

    // Version reducida del nivel, para las casillas de la pantalla de seleccion.
    public static void dibujarMiniatura(Graphics2D g, Nivel nivel, int x, int y, int ancho, int alto) {
        Shape clipAntes = g.getClip();
        AffineTransform transformeAntes = g.getTransform();

        g.clipRect(x, y, ancho, alto);
        g.translate(x, y);
        g.scale(ancho / (double) Constantes.ANCHO, alto / (double) Constantes.ALTO);

        dibujarFondo(g, nivel);
        dibujarBloques(g, nivel);
        dibujarElementos(g, nivel);
        dibujarPlataformas(g, nivel);

        g.setTransform(transformeAntes);
        g.setClip(clipAntes);
    }

    // El muro solo se ve cuando esta puesto; al abrirlo desaparece del mapa.
    // El que cruza esta siempre, en uno de sus dos sitios: basta con pintar el
    // que en este momento sea solido.
    private static void dibujarMuros(Graphics2D g, Nivel nivel) {
        for (int[] m : nivel.muros) {
            pintarMuro(g, nivel, m[0], m[1], m[2]);
        }
        for (int[] m : nivel.murosCruzan) {
            pintarMuro(g, nivel, m[0], m[1], m[3]);
            pintarMuro(g, nivel, m[0], m[2], m[3]);
        }
    }

    private static void pintarMuro(Graphics2D g, Nivel nivel, int fila, int col, int grupo) {
        if (nivel.mapa[fila][col] != Constantes.BLOQUE) {
            return;
        }
        pintar(g, grupo == 1 ? "pared_clara" : "pared", Constantes.COLOR_BLOQUE,
               col * Constantes.CELDA, fila * Constantes.CELDA,
               Constantes.CELDA, Constantes.CELDA);
    }

    private static void dibujarPlataformas(Graphics2D g, Nivel nivel) {
        for (Plataforma p : nivel.plataformas) {
            if (p.polea) {
                dibujarPolea(g, p);
            } else {
                dibujarTablon(g, p);
            }
        }
    }

    // Cuelga de un solo punto y gira alrededor de el. La cuerda va del anclaje
    // al pivote, y el pivote no se mueve, asi que no se estira.
    private static void dibujarTablon(Graphics2D g, Plataforma p) {
        int centro = Constantes.MARGEN_X + p.pivoteX;
        int cuelga = Constantes.MARGEN_Y + p.pivoteY;
        int techo = cuelga - Constantes.CELDA * 2;

        cuerda(g, centro, techo, centro, cuelga);
        nudo(g, centro, techo);

        AffineTransform antes = g.getTransform();
        g.rotate(p.angulo, centro, cuelga);
        losa(g, p, p.y);
        g.setTransform(antes);
    }

    /**
     * La polea de la maquetacion del nivel 2: la losa cuelga de dos cuerdas
     * que se juntan en un nudo, del nudo sube una hasta el anclaje del techo, y
     * de anclaje a anclaje cruza el nivel entero. La losa se traslada, asi que
     * el tramo vertical se estira en un extremo justo lo que se encoge en el
     * otro; el travesano del techo se pinta una sola vez, desde el extremo que
     * baja.
     */
    private static void dibujarPolea(Graphics2D g, Plataforma p) {
        int centro = Constantes.MARGEN_X + p.pivoteX;
        int arriba = Constantes.MARGEN_Y + p.pivoteY + p.desplazamiento;
        int nudo = arriba - ALTO_NUDO;
        int techo = Constantes.MARGEN_Y + ALTO_CUERDA;

        cuerda(g, centro, nudo, Constantes.MARGEN_X + p.x, arriba);
        cuerda(g, centro, nudo, Constantes.MARGEN_X + p.x + p.ancho, arriba);
        cuerda(g, centro, techo, centro, nudo);
        nudo(g, centro, nudo);
        nudo(g, centro, techo);

        if (p.pareja != null && p.sentidoBase > 0) {
            cuerda(g, centro, techo, Constantes.MARGEN_X + p.pareja.pivoteX, techo);
        }

        losa(g, p, p.y + p.desplazamiento);
    }

    // La losa de ladrillo, sin la cuerda que el sprite trae dibujada encima.
    private static void losa(Graphics2D g, Plataforma p, int y) {
        pintarImagen(g, Recursos.spriteInferior("plataforma", 26), Constantes.COLOR_BLOQUE,
                     p.x, y, p.ancho, p.alto);
    }

    private static void cuerda(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setColor(Constantes.COLOR_BLOQUE);
        g.setStroke(CUERDA);
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(SOLIDO);
    }

    private static void nudo(Graphics2D g, int x, int y) {
        g.setColor(Constantes.COLOR_BLOQUE);
        g.fillOval(x - 6, y - 6, 12, 12);
    }

    private static void dibujarFondo(Graphics2D g, Nivel nivel) {
        BufferedImage fondo = nivel.rutaFondo == null ? null : Recursos.imagen(nivel.rutaFondo);

        g.setColor(Constantes.COLOR_MARCO);
        g.fillRect(0, 0, Constantes.ANCHO, Constantes.ALTO);

        if (fondo != null) {
            // El fondo se ajusta para que su area interior encaje exactamente con
            // la del mapa. En horizontal coinciden las columnas, pero el juego
            // tiene una fila mas de alto, asi que la escala vertical es distinta.
            double escalaX = (Constantes.COLUMNAS - 2) * (double) Constantes.CELDA
                           / (Constantes.COLUMNAS_MAQUETA * Constantes.CELDA_MAQUETA);
            double escalaY = (Constantes.FILAS - 2) * (double) Constantes.CELDA
                           / (Constantes.FILAS_MAQUETA * Constantes.CELDA_MAQUETA);

            int ancho = (int) Math.round(Constantes.ANCHO * escalaX);
            int alto = (int) Math.round(Constantes.ALTO * escalaY);
            int x = (int) Math.round(Constantes.MARGEN_X + Constantes.CELDA
                                     - Constantes.ORIGEN_MAQUETA_X * escalaX);
            int y = (int) Math.round(Constantes.MARGEN_Y + Constantes.CELDA
                                     - Constantes.ORIGEN_MAQUETA_Y * escalaY);
            // ya escalado y guardado: dibujarlo aqui es una copia directa
            g.drawImage(Recursos.escalada(fondo, ancho, alto, nivel.rutaFondo), x, y, null);
            return;
        }

        g.setColor(Constantes.COLOR_FONDO);
        g.fillRect(Constantes.MARGEN_X, Constantes.MARGEN_Y,
                   Constantes.COLUMNAS * Constantes.CELDA,
                   Constantes.FILAS * Constantes.CELDA);

        g.setColor(Constantes.COLOR_REJILLA);
        for (int col = 0; col <= Constantes.COLUMNAS; col++) {
            int x = Constantes.MARGEN_X + col * Constantes.CELDA;
            g.drawLine(x, Constantes.MARGEN_Y, x, Constantes.MARGEN_Y + Constantes.FILAS * Constantes.CELDA);
        }
        for (int fila = 0; fila <= Constantes.FILAS; fila++) {
            int y = Constantes.MARGEN_Y + fila * Constantes.CELDA;
            g.drawLine(Constantes.MARGEN_X, y, Constantes.MARGEN_X + Constantes.COLUMNAS * Constantes.CELDA, y);
        }
    }

    private static void dibujarBloques(Graphics2D g, Nivel nivel) {
        boolean[][] esMuro = new boolean[nivel.filas][nivel.columnas];
        for (int[] m : nivel.muros) {
            esMuro[m[0]][m[1]] = true;
        }
        for (int[] m : nivel.murosCruzan) {
            esMuro[m[0]][m[1]] = true;
            esMuro[m[0]][m[2]] = true;
        }

        g.setColor(Constantes.COLOR_BLOQUE);
        for (int fila = 0; fila < nivel.filas; fila++) {
            for (int col = 0; col < nivel.columnas; col++) {
                if (nivel.mapa[fila][col] == Constantes.BLOQUE && !esMuro[fila][col]) {
                    g.fillRect(Constantes.MARGEN_X + col * Constantes.CELDA,
                               Constantes.MARGEN_Y + fila * Constantes.CELDA,
                               Constantes.CELDA, Constantes.CELDA);
                }
            }
        }
    }

    private static void dibujarElementos(Graphics2D g, Nivel nivel) {
        for (Elemento e : nivel.elementos) {
            if (!e.activo) {
                continue;
            }

            int base = e.y + Constantes.CELDA;

            switch (e.tipo) {
                case Constantes.PUAS:
                    espinas(g, e, base);
                    break;
                case Constantes.LUMINOSIDAD:
                    apoyado(g, "pozo_luminoso", Constantes.COLOR_LUMINOSIDAD, e.x, base, e.ancho, 18);
                    break;
                case Constantes.PENUMBRA:
                    apoyado(g, "pozo_penumbra", Constantes.COLOR_PENUMBRA, e.x, base, e.ancho, 20);
                    break;
                case Constantes.ALBOR:
                    centrado(g, "albor", Constantes.COLOR_LUZ, e, ALTO_CRISTAL);
                    break;
                case Constantes.OBSIDIANA:
                    centrado(g, "obsidiana", Constantes.COLOR_SOMBRA, e, ALTO_CRISTAL);
                    break;
                case Constantes.PUERTA_LUZ:
                    puerta(g, "puerta_luz", Constantes.COLOR_LUMINOSIDAD, e);
                    break;
                case Constantes.PUERTA_SOMBRA:
                    puerta(g, "puerta_sombra", Constantes.COLOR_PENUMBRA, e);
                    break;
                case Constantes.BOTON:
                case Constantes.BOTON_2:
                    apoyado(g, "boton", Constantes.COLOR_BLOQUE, e.x, base, e.ancho, 15);
                    break;
                case Constantes.PALANCA:
                    palanca(g, "palanca", Constantes.COLOR_SOMBRA, e);
                    break;
                case Constantes.PALANCA_2:
                    palanca(g, "palanca_clara", Constantes.COLOR_LUZ, e);
                    break;
                default:
                    break;
            }
        }
    }

    private static void dibujarJugador(Graphics2D g, Jugador j) {
        if (j == null) {
            return;
        }

        BufferedImage img = Recursos.spriteCanvasEscalado(nombreSprite(j), SPRITE_ANCHO, SPRITE_ALTO);
        int x = Constantes.MARGEN_X + j.x + j.ancho / 2 - SPRITE_CENTRO;
        int y = Constantes.MARGEN_Y + j.y + j.alto - SPRITE_BASE;

        if (img == null) {
            g.setColor(j.tipo == Jugador.LUZ ? Constantes.COLOR_LUZ : Constantes.COLOR_SOMBRA);
            g.fillRect(Constantes.MARGEN_X + j.x, Constantes.MARGEN_Y + j.y, j.ancho, j.alto);
            return;
        }

        if (j.mirandoDerecha) {
            g.drawImage(img, x, y, null);
        } else {
            g.drawImage(img, x + SPRITE_ANCHO, y, -SPRITE_ANCHO, SPRITE_ALTO, null);
        }
    }

    // 1-3 caminata, 4 caida encogida, 5 impulso de salto.
    private static String nombreSprite(Jugador j) {
        String base = (j.tipo == Jugador.LUZ) ? "luz" : "sombra";

        if (!j.enSuelo) {
            return base + (j.velY < 0 ? "_5" : "_4");
        }
        if (j.velX != 0) {
            return base + "_" + (j.fotograma + 1);
        }
        return base + "_quieto";
    }

    // Echada se dibuja al reves: la palanca se queda puesta, y asi se ve de un
    // vistazo cual esta ya usada.
    private static void palanca(Graphics2D g, String nombre, Color reserva, Elemento e) {
        int x = Constantes.MARGEN_X + e.x;
        int y = Constantes.MARGEN_Y + e.y + Constantes.CELDA - ALTO_PALANCA;
        BufferedImage img = Recursos.spriteEscalado(nombre, Constantes.CELDA, ALTO_PALANCA);

        if (img == null) {
            g.setColor(reserva);
            g.fillRect(x, y, Constantes.CELDA, ALTO_PALANCA);
        } else if (e.encendida) {
            g.drawImage(img, x + Constantes.CELDA, y, -Constantes.CELDA, ALTO_PALANCA, null);
        } else {
            g.drawImage(img, x, y, null);
        }
    }

    // Una hilera de espinas se pinta en grupos de 3 celdas como mucho, que es lo
    // que mide el sprite; el ultimo grupo lleva las celdas que sobran.
    private static void espinas(Graphics2D g, Elemento e, int base) {
        int paso = CELDAS_ESPINAS * Constantes.CELDA;
        for (int desde = 0; desde < e.ancho; desde += paso) {
            int trozo = Math.min(paso, e.ancho - desde);
            apoyado(g, "espinas", Constantes.COLOR_PUAS, e.x + desde, base, trozo, ALTO_ESPINAS);
        }
    }

    // Apoyado en el borde inferior de la celda y estirado a lo ancho del elemento.
    private static void apoyado(Graphics2D g, String nombre, Color reserva,
                                int x, int base, int ancho, int alto) {
        pintar(g, nombre, reserva, x, base - alto, ancho, alto);
    }

    // Centrado en la celda, conservando la proporcion del sprite.
    private static void centrado(Graphics2D g, String nombre, Color reserva, Elemento e, int alto) {
        BufferedImage img = Recursos.spriteRecortado(nombre);
        int ancho = (img == null) ? alto : alto * img.getWidth() / img.getHeight();

        pintar(g, nombre, reserva,
               e.x + (e.ancho - ancho) / 2,
               e.y + (Constantes.CELDA - alto) / 2,
               ancho, alto);
    }

    // La puerta es mas alta que una celda y se apoya en el suelo.
    private static void puerta(Graphics2D g, String nombre, Color reserva, Elemento e) {
        int alto = ALTO_PUERTA;
        BufferedImage img = Recursos.spriteRecortado(nombre);
        int ancho = (img == null) ? Constantes.CELDA - 8 : alto * img.getWidth() / img.getHeight();

        pintar(g, nombre, reserva,
               e.x + (Constantes.CELDA - ancho) / 2,
               e.y + Constantes.CELDA - alto,
               ancho, alto);
    }

    private static void pintar(Graphics2D g, String nombre, Color reserva,
                               int x, int y, int ancho, int alto) {
        pintarImagen(g, Recursos.spriteEscalado(nombre, ancho, alto), reserva, x, y, ancho, alto);
    }

    private static void pintarImagen(Graphics2D g, BufferedImage img, Color reserva,
                                     int x, int y, int ancho, int alto) {
        if (img == null) {
            g.setColor(reserva);
            g.fillRect(Constantes.MARGEN_X + x, Constantes.MARGEN_Y + y, ancho, alto);
            return;
        }
        // si ya viene al tamano pedido, es una copia directa
        if (img.getWidth() == ancho && img.getHeight() == alto) {
            g.drawImage(img, Constantes.MARGEN_X + x, Constantes.MARGEN_Y + y, null);
        } else {
            g.drawImage(img, Constantes.MARGEN_X + x, Constantes.MARGEN_Y + y, ancho, alto, null);
        }
    }

    private DibujoNivel() {
    }
}
