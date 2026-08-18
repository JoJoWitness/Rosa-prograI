package juego.utilidades;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Recursos {

    private static final HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

    // Devuelve null si el archivo falta: el dibujo tiene color de reserva.
    public static BufferedImage imagen(String ruta) {
        if (cache.containsKey(ruta)) {
            return cache.get(ruta);
        }

        BufferedImage img = null;
        try (InputStream entrada = Assets.abrir(ruta)) {
            if (entrada == null) {
                System.out.println("Falta el archivo " + Assets.archivo(ruta).getAbsolutePath());
            } else {
                img = ImageIO.read(entrada);
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer " + ruta);
        }

        cache.put(ruta, img);
        return img;
    }

    // Lienzo completo de 420x594: se usa para los personajes, para que los
    // fotogramas queden alineados entre si.
    public static BufferedImage sprite(String nombre) {
        return imagen("assets/sprites/" + nombre + ".png");
    }

    // Recortado a su contenido: se usa para los elementos, que se colocan
    // por celda y no se animan.
    public static BufferedImage spriteRecortado(String nombre) {
        String clave = "recorte:" + nombre;
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }

        BufferedImage completo = sprite(nombre);
        BufferedImage recorte = completo == null ? null : recortar(completo);
        cache.put(clave, recorte);
        return recorte;
    }

    // Parte inferior del sprite recortado. La plataforma trae la cuerda dibujada
    // encima, y aqui solo interesa la losa de ladrillo.
    public static BufferedImage spriteInferior(String nombre, int porcentaje) {
        String clave = "abajo:" + nombre + ":" + porcentaje;
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }

        BufferedImage recorte = spriteRecortado(nombre);
        BufferedImage trozo = null;

        if (recorte != null) {
            int alto = Math.max(1, recorte.getHeight() * porcentaje / 100);
            trozo = recorte.getSubimage(0, recorte.getHeight() - alto, recorte.getWidth(), alto);
        }

        cache.put(clave, trozo);
        return trozo;
    }

    // Imagen ya escalada al tamano en que se dibuja. Escalar un fondo de
    // 1920x1080 en cada frame cuesta 14 ms; hacerlo una vez y guardarlo, 0.
    public static BufferedImage escalada(BufferedImage original, int ancho, int alto, String clave) {
        String k = "escala:" + clave + ":" + ancho + "x" + alto;
        if (cache.containsKey(k)) {
            return cache.get(k);
        }

        BufferedImage destino = null;
        if (original != null && ancho > 0 && alto > 0) {
            destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = destino.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, ancho, alto, null);
            g.dispose();
        }

        cache.put(k, destino);
        return destino;
    }

    /** Sprite recortado y ya escalado al tamano final. */
    public static BufferedImage spriteEscalado(String nombre, int ancho, int alto) {
        return escalada(spriteRecortado(nombre), ancho, alto, "rec:" + nombre);
    }

    /** Lienzo completo del personaje, ya escalado. */
    public static BufferedImage spriteCanvasEscalado(String nombre, int ancho, int alto) {
        return escalada(sprite(nombre), ancho, alto, "canvas:" + nombre);
    }

    public static BufferedImage fondo(String nombre) {
        return imagen("assets/fondos/" + nombre + ".png");
    }

    private static BufferedImage recortar(BufferedImage img) {
        int minX = img.getWidth();
        int minY = img.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alfa = (img.getRGB(x, y) >>> 24) & 0xFF;
                if (alfa > 10) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < 0) {
            return img;
        }
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private Recursos() {
    }
}
