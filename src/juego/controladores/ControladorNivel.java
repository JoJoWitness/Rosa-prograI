package juego.controladores;

import java.awt.Rectangle;

import juego.objetos.Elemento;
import juego.objetos.Jugador;
import juego.objetos.Nivel;
import juego.objetos.Plataforma;
import juego.utilidades.Colisiones;
import juego.utilidades.Constantes;
import juego.utilidades.Sonidos;

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
        boolean[] placas = new boolean[Constantes.RANURAS];
        boolean[] palancas = new boolean[Constantes.RANURAS];
        leerInterruptores(placas, palancas);

        actualizarMuros(placas, palancas);
        actualizarPlataformas();
    }

    /**
     * Que grupos estan activados ahora mismo, y de paso el sonido de las placas.
     * Se lee siempre, aunque el nivel no tenga ningun muro que abrir: el nivel 3
     * tiene un boton que no manda sobre nada y aun asi tiene que sonar al
     * pisarlo.
     */
    private void leerInterruptores(boolean[] placas, boolean[] palancas) {
        for (Elemento e : nivel.elementos) {
            if (e.grupo < 0 || e.grupo >= Constantes.GRUPOS) {
                continue;
            }
            if (Constantes.esBoton(e.tipo)) {
                boolean pisado = pisa(luz, e) || pisa(sombra, e);
                if (pisado && !e.pisado) {
                    Sonidos.reproducir(Sonidos.BOTON);   // al pisarla, no mientras
                }
                e.pisado = pisado;
                if (pisado) {
                    placas[e.grupo] = true;              // mientras alguien la pise
                }
            }
            if (Constantes.esPalanca(e.tipo) && e.encendida) {
                palancas[e.grupo] = true;                // y se queda echada
            }
        }
    }

    /**
     * Botones y palancas quitan el muro de ladrillos de su grupo mientras estan
     * activados. El muro se pone y se quita del mapa de colisiones. El muro
     * doble ('D') solo se quita con todos los grupos activados a la vez.
     */
    private void actualizarMuros(boolean[] placas, boolean[] palancas) {
        if (nivel.muros.isEmpty() && nivel.murosCruzan.isEmpty()) {
            return;
        }

        boolean[] cualquiera = new boolean[Constantes.RANURAS];
        for (int g = 0; g < Constantes.GRUPOS; g++) {
            cualquiera[g] = placas[g] || palancas[g];

            // El paso a la sala de las puertas lo abre CUALQUIER placa pisada, y
            // solo mientras se pisa. Es el mismo trato que en el nivel 1, donde
            // hay una placa a cada lado del muro: uno pisa la suya y el otro
            // cruza. Las palancas no cuentan aqui, porque quedan echadas para
            // siempre y dejarian el paso abierto.
            cualquiera[Constantes.GRUPO_AMBOS] |= placas[g];
        }

        for (int[] m : nivel.muros) {
            int fila = m[0], col = m[1], grupo = m[2];
            nivel.mapa[fila][col] = Constantes.muroQuitado(grupo, cualquiera)
                ? Constantes.VACIO : Constantes.BLOQUE;
        }

        // El muro que cruza no se quita: cambia de sitio. Sale de un lado del
        // pilar y aparece al otro, asi que deja de ser escalon del pozo de uno
        // para serlo del pozo del otro. Es lo que obliga a cooperar: cada
        // palanca esta en un pozo y le abre el paso al de enfrente.
        // Los muros de la torre son cosa de las palancas, no de las placas: son
        // un cambio que tiene que quedarse puesto mientras el otro sube.
        for (int[] m : nivel.murosCruzan) {
            boolean cruzado = Constantes.muroQuitado(m[3], palancas);
            nivel.mapa[m[0]][m[1]] = cruzado ? Constantes.VACIO : Constantes.BLOQUE;
            nivel.mapa[m[0]][m[2]] = cruzado ? Constantes.BLOQUE : Constantes.VACIO;
        }
    }

    /**
     * Las plataformas cuelgan de su centro, asi que el peso no las puede bajar:
     * solo inclinarlas. En cuanto alguien se sube, el tablon empieza a girar
     * hacia su lado y no para; pasado ANG_CAIDA deja de sostener y el que va
     * encima se cae. Al quedarse sola vuelve sola a la horizontal.
     *
     * **Cada tablon va por su cuenta.** No hay grupos ni parejas: solo cuenta
     * quien esta encima de el. Antes iban acoplados por grupo y pisar uno movia
     * al otro, que es lo que hacia que en el nivel 2 las dos plataformas 'E'
     * bajaran a la vez estando en extremos opuestos del mapa.
     *
     * El tablon cae hacia el lado por el que le tira el peso, asi que ponerse a
     * un lado o al otro del punto del que cuelga cambia hacia donde vuelca.
     * Pero no es un balancin: la inclinacion no se queda a medias segun lo
     * lejos que este el peso del pivote, sigue cayendo hasta el tope.
     */
    private void actualizarPlataformas() {
        for (Plataforma p : nivel.plataformas) {
            boolean llevaLuz = vaEncima(luz, p);
            boolean llevaSombra = vaEncima(sombra, p);

            p.girar(ladoDelPeso(p, llevaLuz, llevaSombra));

            // Al que va de pie encima se le pega a la nueva superficie: si no,
            // se queda flotando cuando el tablon baja y lo atraviesa al subir.
            if (llevaLuz) {
                posarEn(luz, p);
            }
            if (llevaSombra) {
                posarEn(sombra, p);
            }
        }
    }

    /**
     * Hacia que lado tira el peso: +1 si cae a la derecha del punto del que
     * cuelga el tablon, -1 si a la izquierda, 0 si no hay nadie encima.
     */
    private int ladoDelPeso(Plataforma p, boolean llevaLuz, boolean llevaSombra) {
        if (!llevaLuz && !llevaSombra) {
            return 0;
        }

        int desvio = 0;
        if (llevaLuz) {
            desvio += (luz.x + luz.ancho / 2) - p.pivoteX;
        }
        if (llevaSombra) {
            desvio += (sombra.x + sombra.ancho / 2) - p.pivoteX;
        }

        // Justo encima del pivote, o los dos repartidos a partes iguales, no
        // frena el vuelco: cae hacia el lado que le toca por simbolo.
        return desvio != 0 ? Integer.signum(desvio) : p.sentidoBase;
    }

    private void posarEn(Jugador j, Plataforma p) {
        if (p.sujeta()) {
            j.y = p.alturaEn(muestra(j, p)) - j.alto;
        }
    }

    // El mismo criterio que Colisiones: el centro, pero recortado al tablon.
    private static int muestra(Jugador j, Plataforma p) {
        int centro = j.x + j.ancho / 2;
        return Math.max(p.x, Math.min(centro, p.x + p.ancho - 1));
    }

    // De pie encima: el centro dentro del tablon (el mismo criterio que usa
    // Colisiones, para que no discrepen) y los pies en la superficie.
    private boolean vaEncima(Jugador j, Plataforma p) {
        if (j.x + j.ancho <= p.x || j.x >= p.x + p.ancho) {
            return false;
        }
        return Math.abs((j.y + j.alto) - p.alturaEn(muestra(j, p)))
            <= Constantes.PEGADO_TABLON;
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
        if (!e.encendida) {
            e.encendida = true;
            Sonidos.reproducir(Sonidos.PALANCA);   // al echarla, no mientras se toca
        }
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
                        recoger(j, e, Sonidos.ALBOR);
                    }
                    break;
                case Constantes.OBSIDIANA:
                    if (j.tipo == Jugador.SOMBRA) {
                        recoger(j, e, Sonidos.OBSIDIANA);
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

    // Cada cristal suena distinto: el albor es cosa de Luz y la obsidiana de
    // Sombra, asi que el sonido dice de quien fue la recogida sin mirar.
    private void recoger(Jugador j, Elemento e, String sonido) {
        e.activo = false;
        j.recogidos++;
        Sonidos.reproducir(sonido);
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
