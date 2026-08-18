package juego.controladores;

import java.awt.Rectangle;

import juego.objetos.Elemento;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.objetos.Plataforma;
import juego.utilidades.Colisiones;
import juego.utilidades.Constantes;

public class ControladorNivel {

    // Se compara con la hitbox encogida: si no, se muere al rozar un borde.
    private static final int MARGEN_CONTACTO = 8;

    public final Nivel nivel;
    public final Jugador luz;
    public final Jugador sombra;

    public ControladorNivel(Nivel nivel) {
        this.nivel = nivel;
        this.luz = new Jugador(Jugador.LUZ, nivel.luzX, nivel.luzY);
        this.sombra = new Jugador(Jugador.SOMBRA, nivel.sombraX, nivel.sombraY);
    }

    public void actualizar() {
        actualizarMecanismos();

        Colisiones.mover(luz, nivel.mapa, nivel.plataformas);
        Colisiones.mover(sombra, nivel.mapa, nivel.plataformas);

        revisarElementos(luz);
        revisarElementos(sombra);

        animar(luz);
        animar(sombra);
    }

    private void actualizarMecanismos() {
        actualizarMuros();
        actualizarPlataformas();
    }

    /**
     * Botones y palancas quitan el muro de ladrillos de su grupo mientras estan
     * activados. El muro se pone y se quita del mapa de colisiones. El muro
     * doble ('D') solo se quita con todos los grupos activados a la vez.
     */
    private void actualizarMuros() {
        if (nivel.muros.isEmpty()) {
            return;
        }

        boolean[] abierto = new boolean[Constantes.GRUPOS];
        for (Elemento e : nivel.elementos) {
            if (e.grupo < 0 || e.grupo >= Constantes.GRUPOS) {
                continue;
            }
            if (Constantes.esBoton(e.tipo) && (pisa(luz, e) || pisa(sombra, e))) {
                abierto[e.grupo] = true;      // mientras alguien la pise
            }
            if (Constantes.esPalanca(e.tipo) && e.encendida) {
                abierto[e.grupo] = true;
            }
        }

        for (int[] m : nivel.muros) {
            int fila = m[0], col = m[1], grupo = m[2];
            nivel.mapa[fila][col] = Constantes.muroQuitado(grupo, abierto)
                ? Constantes.VACIO : Constantes.BLOQUE;
        }
    }

    /**
     * Las plataformas cuelgan en el aire y las mueve el peso, no los botones:
     * bajan mientras alguien esta encima y vuelven a subir al quedarse solas.
     * El contrapeso del mismo grupo hace lo contrario.
     */
    private void actualizarPlataformas() {
        if (nivel.plataformas.isEmpty()) {
            return;
        }

        boolean[] conPeso = new boolean[Constantes.GRUPOS];
        for (Plataforma p : nivel.plataformas) {
            if (p.grupo >= 0 && p.grupo < Constantes.GRUPOS
                && (vaEncima(luz, p) || vaEncima(sombra, p))) {
                conPeso[p.grupo] = true;
            }
        }

        for (Plataforma p : nivel.plataformas) {
            boolean activada = p.grupo >= 0 && p.grupo < Constantes.GRUPOS && conPeso[p.grupo];
            boolean llevaLuz = vaEncima(luz, p);
            boolean llevaSombra = vaEncima(sombra, p);

            p.mover(activada);

            // Arrastrar a quien iba encima, o se queda flotando.
            if (llevaLuz) {
                luz.y += p.deltaY;
            }
            if (llevaSombra) {
                sombra.y += p.deltaY;
            }
        }
    }

    private boolean vaEncima(Jugador j, Plataforma p) {
        int pies = j.y + j.alto;
        return Math.abs(pies - p.y) <= 2
            && j.x + j.ancho > p.x
            && j.x < p.x + p.ancho;
    }

    private boolean pisa(Jugador j, Elemento e) {
        return j.getRectangulo(MARGEN_CONTACTO).intersects(e.getRectangulo());
    }

    /**
     * La palanca se echa al tocarla y **se queda echada**: mantiene lo que ha
     * hecho aunque el que la toco se vaya. Eso es lo que la separa de la placa
     * de presion, que solo vale mientras alguien la pisa. Si alternara, pasar
     * otra vez por encima desharia el paso abierto.
     */
    private void revisarPalanca(Elemento e) {
        e.encendida = true;
    }

    // El fotograma avanza aqui, no al dibujar: paintComponent solo lee estado.
    private void animar(Jugador j) {
        if (j.enSuelo && j.velX != 0) {
            j.contadorAnim++;
            if (j.contadorAnim >= 5) {
                j.contadorAnim = 0;
                j.fotograma = (j.fotograma + 1) % 3;
            }
        } else {
            j.contadorAnim = 0;
            j.fotograma = 0;
        }
    }

    private void revisarElementos(Jugador j) {
        Rectangle cuerpo = j.getRectangulo(MARGEN_CONTACTO);

        for (Elemento e : nivel.elementos) {
            if (!e.activo || !cuerpo.intersects(e.getRectangulo())) {
                continue;
            }

            switch (e.tipo) {
                case Constantes.PUAS:
                    j.vivo = false;
                    break;
                case Constantes.LUMINOSIDAD:
                    // La Luminosidad extingue a Sombra; Luz la atraviesa.
                    if (j.tipo == Jugador.SOMBRA) {
                        j.vivo = false;
                    }
                    break;
                case Constantes.PENUMBRA:
                    // La Penumbra apaga a Luz; Sombra la atraviesa.
                    if (j.tipo == Jugador.LUZ) {
                        j.vivo = false;
                    }
                    break;
                case Constantes.ALBOR:
                    if (j.tipo == Jugador.LUZ) {
                        e.activo = false;
                        j.recogidos++;
                    }
                    break;
                case Constantes.OBSIDIANA:
                    if (j.tipo == Jugador.SOMBRA) {
                        e.activo = false;
                        j.recogidos++;
                    }
                    break;
                case Constantes.PALANCA:
                case Constantes.PALANCA_2:
                    revisarPalanca(e);
                    break;
                default:
                    break;
            }
        }
    }

    public boolean hayMuerto() {
        return !luz.vivo || !sombra.vivo;
    }

    // Se comprueba cada frame, sin banderas: si uno se sale de su puerta,
    // deja de contar.
    public boolean hanGanado() {
        return enSuPuerta(luz, Constantes.PUERTA_LUZ)
            && enSuPuerta(sombra, Constantes.PUERTA_SOMBRA);
    }

    private boolean enSuPuerta(Jugador j, char tipoPuerta) {
        int centro = j.x + j.ancho / 2;
        int pies = j.y + j.alto;

        for (Elemento e : nivel.elementos) {
            if (e.tipo != tipoPuerta) {
                continue;
            }
            if (centro >= e.x && centro < e.x + Constantes.CELDA
                && pies > e.y && pies <= e.y + Constantes.CELDA) {
                return true;
            }
        }
        return false;
    }
}
