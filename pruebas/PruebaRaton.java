import java.awt.Component;
import java.awt.event.MouseEvent;

import juego.controladores.ControladorRaton;
import juego.utilidades.Boton;

/**
 * El juego se dibuja sobre 1920x1080 pero la ventana es mas pequena, porque en
 * una pantalla de 1920x1080 no cabe entera con su marco. Aqui se comprueba que
 * el clic sigue cayendo donde el jugador ve el boton, y no donde estaria si la
 * ventana midiera 1920x1080.
 *
 * Se comprueba tambien con margenes: la imagen conserva siempre su proporcion
 * 16:9, asi que si al panel le sobra sitio por un lado, el juego queda centrado
 * y el clic tiene que descontar ese margen.
 */
public class PruebaRaton {

    static int fallos = 0;
    static Component c = new java.awt.Canvas();

    static void ok(String n, boolean b, String d) {
        System.out.println((b ? "  OK   " : "  FALLA") + "  " + n + "   " + d);
        if (!b) fallos++;
    }

    static MouseEvent clic(int x, int y) {
        return new MouseEvent(c, MouseEvent.MOUSE_PRESSED, System.nanoTime(),
                              0, x, y, 1, false);
    }

    public static void main(String[] a) {
        // El boton JUGAR del menu, tal y como esta colocado en el juego.
        Boton jugar = new Boton(620, 790, 680, 78, "JUGAR");
        double escala = 0.8963;                       // lo que sale en 1920x1080

        // El centro del boton, ya encogido: es donde lo ve y lo pincha el jugador.
        int x = (int) Math.round(960 * escala);
        int y = (int) Math.round(829 * escala);

        ControladorRaton sinAjustar = new ControladorRaton();
        sinAjustar.mousePressed(clic(x, y));
        ok("sin ajustar la escala el clic se pierde", !sinAjustar.clicEn(jugar),
           "es el fallo que hay que evitar");

        ControladorRaton raton = new ControladorRaton();
        raton.ajustarVista(escala, 0, 0);
        raton.mousePressed(clic(x, y));
        ok("el clic cae en el boton", raton.clicEn(jugar), "");

        raton.consumir();
        raton.mouseMoved(new MouseEvent(c, MouseEvent.MOUSE_MOVED, System.nanoTime(),
                                        0, x, y, 0, false));
        ok("el raton por encima tambien", raton.encima(jugar), "es lo que ilumina el boton");

        // Justo fuera del boton por arriba: la esquina no se debe comer un pixel de mas.
        raton.mouseMoved(new MouseEvent(c, MouseEvent.MOUSE_MOVED, System.nanoTime(),
                                        0, (int) Math.round(960 * escala),
                                        (int) Math.round(700 * escala), 0, false));
        ok("fuera del boton no cuenta", !raton.encima(jugar), "");

        // A tamano real la cuenta no debe cambiar nada.
        ControladorRaton entero = new ControladorRaton();
        entero.ajustarVista(1, 0, 0);
        entero.mousePressed(clic(960, 829));
        ok("a escala 1 sigue igual", entero.clicEn(jugar), "");

        // Panel mas ancho de la cuenta: la imagen no se estira, se queda
        // centrada y sobra color de marco a los lados.
        double estrecha = 0.5;
        int margen = 200;
        ControladorRaton conMargen = new ControladorRaton();
        conMargen.ajustarVista(estrecha, margen, 0);
        conMargen.mousePressed(clic(margen + (int) Math.round(960 * estrecha),
                                    (int) Math.round(829 * estrecha)));
        ok("con margen el clic tambien cae", conMargen.clicEn(jugar),
           "la imagen va centrada, no estirada");

        conMargen.consumir();
        conMargen.mousePressed(clic((int) Math.round(960 * estrecha),
                                    (int) Math.round(829 * estrecha)));
        ok("sin descontar el margen no caeria", !conMargen.clicEn(jugar), "");

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN"
                                       : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
