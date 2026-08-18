package juego.controladores;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;

public class ControladorTeclado implements KeyListener, FocusListener {

    // Guarda ademas el orden de pulsacion: hace falta para saber cual gana
    // cuando se tienen las dos direcciones apretadas a la vez.
    private final HashMap<Integer, Long> presionadas = new HashMap<Integer, Long>();
    private final HashSet<Integer> pulsadasEnElTick = new HashSet<Integer>();
    private long contador;

    public boolean estaPresionada(int codigo) {
        return presionadas.containsKey(codigo);
    }

    /**
     * Si la tecla se pulso en algun momento desde el tick anterior, aunque ya
     * se haya soltado. Sin esto se pierden los toques cortos: el bucle mira el
     * teclado 60 veces por segundo y un toque puede caer entre dos miradas.
     */
    public boolean fuePulsada(int codigo) {
        return pulsadasEnElTick.contains(codigo);
    }

    /** Se llama al final de cada tick, cuando ya se ha leido todo. */
    public void nuevoTick() {
        pulsadasEnElTick.clear();
    }

    /** De dos teclas apretadas a la vez, la que se pulso mas tarde. */
    public int ultimaEntre(int a, int b) {
        Long ta = presionadas.get(a);
        Long tb = presionadas.get(b);
        if (ta == null) return b;
        if (tb == null) return a;
        return ta >= tb ? a : b;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // El auto-repeat repite keyPressed; no se reordena si ya estaba apretada.
        if (!presionadas.containsKey(e.getKeyCode())) {
            presionadas.put(e.getKeyCode(), ++contador);
        }
        pulsadasEnElTick.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        presionadas.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Sin esto las teclas se quedan "pegadas" al perder el foco.
    @Override
    public void focusLost(FocusEvent e) {
        presionadas.clear();
        pulsadasEnElTick.clear();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }
}
