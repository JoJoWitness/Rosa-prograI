import java.awt.event.KeyEvent;
import java.awt.Component;
import juego.controladores.ControladorTeclado;

public class PruebaTeclado {
    static int fallos = 0;
    static Component c = new java.awt.Canvas();
    static void ok(String n, boolean b, String d) {
        System.out.println((b?"  OK   ":"  FALLA")+"  "+n+"   "+d); if(!b) fallos++;
    }
    static KeyEvent ev(int codigo, int id) {
        return new KeyEvent(c, id, System.nanoTime(), 0, codigo, (char)codigo);
    }
    public static void main(String[] a) {
        ControladorTeclado t = new ControladorTeclado();
        int A = KeyEvent.VK_A, D = KeyEvent.VK_D;

        t.keyPressed(ev(D, KeyEvent.KEY_PRESSED));
        ok("D apretada", t.estaPresionada(D), "");
        ok("con solo D gana D", t.ultimaEntre(A, D) == D, "");

        t.keyPressed(ev(A, KeyEvent.KEY_PRESSED));
        ok("con D y luego A gana A", t.ultimaEntre(A, D) == A, "es lo que quita la sensacion de tecla pegada");

        // el auto-repeat repite keyPressed de D: no debe robarle el turno a A
        t.keyPressed(ev(D, KeyEvent.KEY_PRESSED));
        t.keyPressed(ev(D, KeyEvent.KEY_PRESSED));
        ok("el auto-repeat no reordena", t.ultimaEntre(A, D) == A, "");

        t.keyReleased(ev(A, KeyEvent.KEY_RELEASED));
        ok("al soltar A vuelve D", t.ultimaEntre(A, D) == D, "");
        ok("A ya no esta apretada", !t.estaPresionada(A), "");

        t.keyPressed(ev(A, KeyEvent.KEY_PRESSED));
        ok("volver a apretar A la hace ganar", t.ultimaEntre(A, D) == A, "");

        t.focusLost(null);
        ok("perder el foco suelta todo", !t.estaPresionada(A) && !t.estaPresionada(D), "");

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN" : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
