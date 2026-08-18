package juego;

import javax.swing.SwingUtilities;

import juego.controladores.JuegoControlador;

public class Main {

    public static void main(String[] args) {
        // Java2D por software tarda 15 ms en volcar la ventana; con OpenGL, 5.
        // Hay que fijarlo antes de tocar nada de AWT.
        System.setProperty("sun.java2d.opengl", "true");
        // La ventana mide 1920x1080 y no lleva bordes. Si el sistema tiene el
        // escalado de pantalla al 125% o al 150%, sin esto Java la dibuja mas
        // grande que el monitor. Al abrir un jar con doble clic no hay forma de
        // pasar -D, asi que se fija aqui.
        System.setProperty("sun.java2d.uiScale", "1");

        SwingUtilities.invokeLater(() -> new JuegoControlador().iniciar());
    }
}
