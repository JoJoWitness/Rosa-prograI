package juego.controladores;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.Timer;

import juego.dibujo.DibujoHUD;
import juego.dibujo.DibujoMenu;
import juego.dibujo.DibujoNivel;
import juego.dibujo.Lienzo;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.utilidades.Boton;
import juego.utilidades.CargadorNivel;
import juego.utilidades.Constantes;

public class JuegoControlador {

    private static final int TOTAL_NIVELES = 4;
    private static final int CASILLAS = 5;

    private JFrame ventana;
    private Lienzo lienzo;
    private ControladorTeclado teclado;
    private ControladorRaton raton;

    private ControladorNivel partida;
    private Estado estado = Estado.MENU;

    private int nivelActual;
    // Todos abiertos desde el principio: se puede entrar a cualquier nivel
    // sin tener que superar el anterior.
    private int desbloqueados = TOTAL_NIVELES;
    private int paginaTutorial;
    private boolean reinicioPendiente;

    private boolean verRejilla;
    private boolean gPulsada;

    private long ultimoFrame;
    private double fps = 60;

    private final Nivel[] previos = new Nivel[CASILLAS];

    private Boton btnJugar;
    private Boton btnTutorial;
    private Boton[] btnCasillas;
    private Boton btnAnterior;
    private Boton btnSiguiente;
    private Boton btnVolver;
    private Boton btnReintentar;
    private Boton btnReanudar;
    private Boton btnSalir;
    private Boton btnSeguir;
    private Boton btnPausa;

    public void iniciar() {
        crearBotones();
        cargarPrevios();

        teclado = new ControladorTeclado();
        raton = new ControladorRaton();

        lienzo = new Lienzo(this);
        lienzo.addKeyListener(teclado);
        lienzo.addFocusListener(teclado);
        lienzo.addMouseListener(raton);
        lienzo.addMouseMotionListener(raton);

        // Sin foco no llega ninguna tecla. En vez de dejar al jugador dandole
        // a teclas muertas, se pausa; y cualquier clic devuelve el foco.
        lienzo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (estado == Estado.JUGANDO) {
                    estado = Estado.PAUSA;
                }
            }
        });
        lienzo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!lienzo.isFocusOwner()) {
                    lienzo.requestFocusInWindow();
                }
            }
        });

        ventana = new JFrame("Luz y Sombra");
        ventana.setUndecorated(true);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.add(lienzo);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        ventana.toFront();

        // Despues de setVisible: si no, el panel no recibe el teclado.
        lienzo.requestFocusInWindow();

        new Timer(Constantes.MS_POR_FRAME, e -> actualizar()).start();
    }

    private void crearBotones() {
        btnJugar = new Boton(620, 790, 680, 78, "JUGAR");
        btnTutorial = new Boton(620, 918, 680, 78, "TUTORIAL");

        btnCasillas = new Boton[CASILLAS];
        int ancho = 428;
        int alto = 272;
        btnCasillas[0] = new Boton(128, 330, ancho, alto, "1");
        btnCasillas[1] = new Boton(724, 330, ancho, alto, "2");
        btnCasillas[2] = new Boton(1320, 330, ancho, alto, "3");
        btnCasillas[3] = new Boton(380, 700, ancho, alto, "4");
        btnCasillas[4] = new Boton(1068, 700, ancho, alto, "5");

        btnAnterior = new Boton(30, 505, 120, 110, "");
        btnSiguiente = new Boton(1780, 505, 120, 110, "");
        btnVolver = new Boton(760, 990, 400, 70, "VOLVER");

        btnReintentar = new Boton(285, 620, 540, 84, "REINTENTAR");
        btnReanudar = new Boton(1095, 620, 540, 84, "REANUDAR");
        btnSalir = new Boton(690, 790, 540, 84, "SALIR");
        btnSeguir = new Boton(690, 620, 540, 84, "CONTINUAR");

        btnPausa = new Boton(Constantes.MARGEN_X + 18, Constantes.MARGEN_Y + 16, 46, 46, "");
    }

    // Los previos de las casillas se cargan una vez, no en cada frame.
    private void cargarPrevios() {
        for (int i = 0; i < TOTAL_NIVELES; i++) {
            previos[i] = CargadorNivel.cargar(rutaNivel(i), rutaFondo(i));
        }
    }

    private String rutaNivel(int indice) {
        return "assets/niveles/nivel" + (indice + 1) + ".txt";
    }

    private String rutaFondo(int indice) {
        return "assets/fondos/nivel" + (indice + 1) + ".png";
    }

    // ---------- bucle ----------

    private void actualizar() {
        long ahora = System.nanoTime();
        if (ultimoFrame != 0) {
            double actual = 1e9 / Math.max(1, ahora - ultimoFrame);
            fps = fps * 0.9 + actual * 0.1;      // media suavizada
        }
        ultimoFrame = ahora;

        // Recargar a mitad del tick deja referencias viejas.
        if (reinicioPendiente) {
            reinicioPendiente = false;
            empezarNivel(nivelActual);
        }

        switch (estado) {
            case MENU:      entradaMenu();      break;
            case NIVELES:   entradaNiveles();   break;
            case TUTORIAL:  entradaTutorial();  break;
            case JUGANDO:   jugar();            break;
            case PAUSA:     entradaPausa();     break;
            case GANADO:    entradaGanado();    break;
            case PERDIDO:   entradaPerdido();   break;
            default: break;
        }

        // G alterna la rejilla de depuracion, solo al pulsar y no mientras se mantiene
        boolean g = teclado.estaPresionada(KeyEvent.VK_G);
        if (g && !gPulsada) {
            verRejilla = !verRejilla;
        }
        gPulsada = g;

        raton.consumir();
        teclado.nuevoTick();
        lienzo.pintarYa();
    }

    private void jugar() {
        if (teclado.estaPresionada(KeyEvent.VK_ESCAPE) || raton.clicEn(btnPausa)) {
            estado = Estado.PAUSA;
            return;
        }
        if (teclado.estaPresionada(KeyEvent.VK_R)) {
            reinicioPendiente = true;
            return;
        }

        mover(partida.luz, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W);
        mover(partida.sombra, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP);

        partida.actualizar();

        if (partida.hayMuerto()) {
            estado = Estado.PERDIDO;
        } else if (partida.hanGanado()) {
            estado = Estado.GANADO;
        }
    }

    private void mover(Jugador j, int izquierda, int derecha, int saltar) {
        boolean izq = teclado.estaPresionada(izquierda);
        boolean der = teclado.estaPresionada(derecha);

        // Con las dos apretadas manda la ultima pulsada. Si gana siempre la
        // misma, cambiar de sentido sin soltar se siente como si la tecla se
        // quedara pegada.
        if (izq && der) {
            if (teclado.ultimaEntre(izquierda, derecha) == derecha) {
                izq = false;
            } else {
                der = false;
            }
        }

        j.velX = 0;
        if (izq) {
            j.velX = -Constantes.VEL_X;
            j.mirandoDerecha = false;
        } else if (der) {
            j.velX = Constantes.VEL_X;
            j.mirandoDerecha = true;
        }

        // vale tanto mantenerla como darle un toque corto entre dos ticks
        if (teclado.estaPresionada(saltar) || teclado.fuePulsada(saltar)) {
            j.saltar();
        }
    }

    private void entradaMenu() {
        if (raton.clicEn(btnJugar)) {
            estado = Estado.NIVELES;
        } else if (raton.clicEn(btnTutorial)) {
            paginaTutorial = 0;
            estado = Estado.TUTORIAL;
        } else if (teclado.estaPresionada(KeyEvent.VK_ESCAPE)) {
            // La ventana no tiene decoracion: sin esto no habria forma de salir.
            System.exit(0);
        }
    }

    private void entradaNiveles() {
        for (int i = 0; i < TOTAL_NIVELES; i++) {
            if (i < desbloqueados && raton.clicEn(btnCasillas[i])) {
                empezarNivel(i);
                return;
            }
        }
        if (teclado.estaPresionada(KeyEvent.VK_ESCAPE)) {
            estado = Estado.MENU;
        }
    }

    private void entradaTutorial() {
        if (paginaTutorial > 0 && raton.clicEn(btnAnterior)) {
            paginaTutorial--;
        } else if (paginaTutorial < 2 && raton.clicEn(btnSiguiente)) {
            paginaTutorial++;
        } else if (raton.clicEn(btnVolver) || teclado.estaPresionada(KeyEvent.VK_ESCAPE)) {
            estado = Estado.MENU;
        }
    }

    private void entradaPausa() {
        if (raton.clicEn(btnReanudar)) {
            estado = Estado.JUGANDO;
        } else if (raton.clicEn(btnReintentar)) {
            reinicioPendiente = true;
        } else if (raton.clicEn(btnSalir)) {
            estado = Estado.MENU;
        }
    }

    private void entradaPerdido() {
        if (raton.clicEn(btnReintentar) || teclado.estaPresionada(KeyEvent.VK_R)) {
            reinicioPendiente = true;
        } else if (raton.clicEn(btnSalir)) {
            estado = Estado.NIVELES;
        }
    }

    private void entradaGanado() {
        if (raton.clicEn(btnSeguir)) {
            estado = Estado.NIVELES;
        } else if (raton.clicEn(btnSalir)) {
            estado = Estado.MENU;
        }
    }

    private void empezarNivel(int indice) {
        nivelActual = indice;
        partida = new ControladorNivel(CargadorNivel.cargar(rutaNivel(indice), rutaFondo(indice)));
        estado = Estado.JUGANDO;
    }

    // ---------- dibujo ----------

    // Solo reparte el dibujo segun el estado; no cambia nada.
    public void pintar(Graphics2D g) {
        int mx = raton.ratonX;
        int my = raton.ratonY;

        switch (estado) {
            case MENU:
                DibujoMenu.menuPrincipal(g, btnJugar, btnTutorial, mx, my);
                break;
            case NIVELES:
                DibujoMenu.seleccionNiveles(g, btnCasillas, previos, desbloqueados, mx, my);
                break;
            case TUTORIAL:
                DibujoMenu.tutorial(g, paginaTutorial, btnAnterior, btnSiguiente, btnVolver, mx, my);
                break;
            default:
                pintarPartida(g, mx, my);
                break;
        }
    }

    private void pintarPartida(Graphics2D g, int mx, int my) {
        if (partida == null) {
            return;
        }

        DibujoNivel.dibujar(g, partida.nivel, partida.luz, partida.sombra);
        if (verRejilla) {
            DibujoNivel.dibujarRejilla(g);
            DibujoHUD.dibujarDiagnostico(g,
                new String[] { "A", "W", "D", "<", "^", ">" },
                new boolean[] {
                    teclado.estaPresionada(KeyEvent.VK_A),
                    teclado.estaPresionada(KeyEvent.VK_W),
                    teclado.estaPresionada(KeyEvent.VK_D),
                    teclado.estaPresionada(KeyEvent.VK_LEFT),
                    teclado.estaPresionada(KeyEvent.VK_UP),
                    teclado.estaPresionada(KeyEvent.VK_RIGHT) },
                fps);
        }
        DibujoHUD.dibujar(g, partida.nivel, partida.luz, partida.sombra, estado);

        if (estado == Estado.PAUSA) {
            DibujoMenu.panelSobreNivel(g, "PAUSA", null,
                new Boton[] { btnReintentar, btnReanudar, btnSalir }, mx, my);
        } else if (estado == Estado.PERDIDO) {
            DibujoMenu.panelSobreNivel(g, "FIN DEL JUEGO", "Vuelve a intentarlo o sal del juego",
                new Boton[] { btnReintentar, btnSalir }, mx, my);
        } else if (estado == Estado.GANADO) {
            DibujoMenu.panelSobreNivel(g, "NIVEL COMPLETADO", resumen(),
                new Boton[] { btnSeguir, btnSalir }, mx, my);
        }
    }

    private String resumen() {
        return "Albor " + partida.luz.recogidos + " / " + partida.nivel.totalAlbores
             + "     Obsidiana " + partida.sombra.recogidos + " / " + partida.nivel.totalObsidianas;
    }
}
