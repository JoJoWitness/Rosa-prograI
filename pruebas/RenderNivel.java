import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import juego.controladores.ControladorNivel;
import juego.dibujo.DibujoNivel;
import juego.objetos.Elemento;
import juego.utilidades.Constantes;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.objetos.Plataforma;
import juego.utilidades.CargadorNivel;

/** Vuelca un nivel a PNG, tal como lo pinta el juego, para compararlo con la maquetacion. */
public class RenderNivel {
    public static void main(String[] args) throws Exception {
        String n = args.length > 0 ? args[0] : "4";
        String salida = args.length > 1 ? args[1] : "/tmp/nivel" + n + ".png";

        Nivel nivel = CargadorNivel.cargar("assets/niveles/nivel" + n + ".txt",
                                           "assets/fondos/nivel" + n + ".png");
        Jugador luz = new Jugador(Jugador.LUZ, nivel.luzX, nivel.luzY);
        Jugador sombra = new Jugador(Jugador.SOMBRA, nivel.sombraX, nivel.sombraY);

        // "palancas": se echan todas y se deja correr un frame, para ver que muros caen
        if (args.length > 2 && args[2].equals("palancas")) {
            ControladorNivel c = new ControladorNivel(nivel);
            for (Elemento e : nivel.elementos) {
                if (Constantes.esPalanca(e.tipo)) e.encendida = true;
            }
            c.actualizar();
        }

        // "polea": la cuerda tirada del todo, para ver el nivel con el extremo
        // de arriba abajo y el de abajo arriba.
        if (args.length > 2 && args[2].equals("polea")) {
            for (Plataforma p : nivel.plataformas) {
                if (!p.polea || p.sentidoBase < 0) continue;
                while (p.desplazamiento < Constantes.RECORRIDO_POLEA) {
                    p.deslizar(Constantes.RECORRIDO_POLEA);
                }
            }
        }

        BufferedImage img = new BufferedImage(Constantes.ANCHO, Constantes.ALTO,
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        DibujoNivel.dibujar(g, nivel, luz, sombra);
        if (args.length > 2 && args[2].equals("rejilla")) {
            DibujoNivel.dibujarRejilla(g);
        }
        g.dispose();

        ImageIO.write(img, "png", new File(salida));
        System.out.println("escrito " + salida);
    }
}
