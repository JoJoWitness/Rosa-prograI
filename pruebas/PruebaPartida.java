import java.util.*;
import juego.controladores.ControladorNivel;
import juego.objetos.*;
import juego.utilidades.*;

/**
 * Juega los niveles de verdad, sobre ControladorNivel y con las mismas teclas
 * que tendria un jugador. No supone nada: si al final hanGanado() no es cierto,
 * el nivel no se puede terminar.
 *
 * Primero **planifica** en simulacion con `Ruta`, encadenando encargos del tipo
 * "este jugador va a este interruptor" o "este jugador va a su puerta". Eso es
 * lo que permite los relevos: uno se queda encima del boton mientras el otro
 * cruza, y luego se cambian. Despues **ejecuta** el plan sobre la partida real,
 * recalculando cada tramo desde donde este el personaje.
 *
 * PruebaJugable dice si existe camino; esta dice si existe una partida.
 */
public class PruebaPartida {

    static final int PROFUNDIDAD = 6;      // encargos como mucho, de sobra para 4 niveles

    static Nivel nivel;
    static ControladorNivel partida;
    static List<Elemento> interruptores;
    static Elemento[] puertas;
    static Map<String,Ruta> cache;
    static int frames;
    static int fallos;

    public static void main(String[] a) {
        for (int i = 1; i <= 4; i++) {
            System.out.println("--- nivel " + i + " ---");
            if (!jugar(i)) fallos++;
        }
        System.out.println(fallos == 0 ? "\nLOS 4 NIVELES SE TERMINAN JUGANDO"
                                       : "\n" + fallos + " NIVEL(ES) NO SE TERMINAN JUGANDO");
        System.exit(fallos == 0 ? 0 : 1);
    }

    static boolean jugar(int numero) {
        nivel = CargadorNivel.cargar("assets/niveles/nivel" + numero + ".txt", "");
        partida = new ControladorNivel(nivel);
        cache = new HashMap<String,Ruta>();
        frames = 0;

        interruptores = new ArrayList<Elemento>();
        for (Elemento e : nivel.elementos) {
            if (Constantes.esBoton(e.tipo) || Constantes.esPalanca(e.tipo)) interruptores.add(e);
        }
        puertas = new Elemento[] { buscar(Constantes.PUERTA_LUZ), buscar(Constantes.PUERTA_SOMBRA) };
        if (puertas[0] == null || puertas[1] == null) {
            System.out.println("  falta alguna puerta");
            return false;
        }

        int[] inicio = { partida.luz.x, partida.luz.y, partida.sombra.x, partida.sombra.y };
        List<int[]> plan = new ArrayList<int[]>();
        if (!planificar(inicio, new boolean[interruptores.size()], new int[] { -1, -1 },
                        PROFUNDIDAD, plan, new HashSet<String>())) {
            System.out.println("  no se encuentra ninguna forma de terminarlo");
            return false;
        }

        for (int[] encargo : plan) {
            System.out.println("  plan: " + nombre(encargo[0]) + " -> "
                + (encargo[1] < 0 ? "su puerta" : "interruptor '" + interruptores.get(encargo[1]).tipo
                                                  + "' del grupo " + interruptores.get(encargo[1]).grupo));
        }
        return ejecutar(plan);
    }

    // ---------- planificacion ----------

    /**
     * Busca una cadena de encargos que deje a los dos en su puerta. Cada encargo
     * es {jugador, objetivo}, con objetivo = indice de interruptor o -1 para la
     * puerta propia. Al moverse, el jugador se levanta del boton que pisara.
     */
    static boolean planificar(int[] pos, boolean[] encendidas, int[] aparcado,
                              int quedan, List<int[]> plan, Set<String> vistas) {
        if (enSuPuerta(pos, Jugador.LUZ) && enSuPuerta(pos, Jugador.SOMBRA)) return true;
        if (quedan == 0) return false;
        if (!vistas.add(situacion(pos, encendidas, aparcado))) return false;

        for (int tipo = 0; tipo <= 1; tipo++) {
            for (int objetivo = -1; objetivo < interruptores.size(); objetivo++) {
                Elemento meta = objetivo < 0 ? puertas[tipo] : interruptores.get(objetivo);
                // Una palanca ya echada se queda echada: ir otra vez no aporta nada.
                if (objetivo >= 0 && Constantes.esPalanca(meta.tipo) && encendidas[objetivo]) continue;

                Ruta r = ruta(tipo, pos, abiertos(encendidas, aparcado, tipo));
                int[] destino = r.postura(objetivo < 0 ? p -> Ruta.enPuerta(p, meta)
                                                       : p -> Ruta.toca(p, meta));
                if (destino == null) continue;

                int[] siguiente = pos.clone();
                siguiente[tipo * 2] = destino[0];
                siguiente[tipo * 2 + 1] = destino[1];
                boolean[] luego = encendidas.clone();
                int[] encima = aparcado.clone();
                encima[tipo] = (objetivo >= 0 && Constantes.esBoton(meta.tipo)) ? objetivo : -1;
                if (objetivo >= 0 && Constantes.esPalanca(meta.tipo)) luego[objetivo] = true;

                plan.add(new int[] { tipo, objetivo });
                if (planificar(siguiente, luego, encima, quedan - 1, plan, vistas)) return true;
                plan.remove(plan.size() - 1);
            }
        }
        return false;
    }

    /** Grupos abiertos para el jugador que se mueve: palancas puestas y el boton que pisa el otro. */
    static boolean[] abiertos(boolean[] encendidas, int[] aparcado, int seMueve) {
        boolean[] abiertos = new boolean[Constantes.GRUPOS];
        for (int i = 0; i < interruptores.size(); i++) {
            Elemento e = interruptores.get(i);
            if (e.grupo < 0 || e.grupo >= Constantes.GRUPOS) continue;
            if (encendidas[i]) abiertos[e.grupo] = true;
        }
        int otro = 1 - seMueve;
        if (aparcado[otro] >= 0) abiertos[interruptores.get(aparcado[otro]).grupo] = true;
        return abiertos;
    }

    static Ruta ruta(int tipo, int[] pos, boolean[] abiertos) {
        String llave = tipo + ":" + pos[tipo * 2] + ":" + pos[tipo * 2 + 1] + ":" + Arrays.toString(abiertos);
        Ruta r = cache.get(llave);
        if (r == null) {
            r = new Ruta(nivel, tipo, abiertos);
            r.explorar(pos[tipo * 2], pos[tipo * 2 + 1]);
            cache.put(llave, r);
        }
        return r;
    }

    static String situacion(int[] pos, boolean[] encendidas, int[] aparcado) {
        return Arrays.toString(pos) + Arrays.toString(encendidas) + Arrays.toString(aparcado);
    }

    static boolean enSuPuerta(int[] pos, int tipo) {
        return Ruta.enPuerta(new int[] { pos[tipo * 2], pos[tipo * 2 + 1] }, puertas[tipo]);
    }

    // ---------- ejecucion ----------

    static boolean ejecutar(List<int[]> plan) {
        for (int[] encargo : plan) {
            int tipo = encargo[0];
            Jugador j = jugador(tipo), otro = otro(j);
            Elemento meta = encargo[1] < 0 ? puertas[tipo] : interruptores.get(encargo[1]);

            aterrizar(j, otro);
            List<int[]> tramo = new Ruta(nivel, tipo, abiertosDeVerdad(otro))
                .buscar(j.x, j.y, encargo[1] < 0 ? p -> Ruta.enPuerta(p, meta) : p -> Ruta.toca(p, meta));
            if (tramo == null) {
                System.out.println("  " + nombre(tipo) + ": ya no hay ruta hasta "
                                   + (encargo[1] < 0 ? "su puerta" : "el interruptor"));
                return false;
            }
            if (!recorrer(j, otro, tramo, nombre(tipo),
                          encargo[1] < 0 ? () -> Ruta.enPuerta(new int[] { j.x, j.y }, meta)
                                         : () -> activado(j, meta))) {
                return false;
            }
            // Acabar la ruta no siempre deja al personaje justo encima: el plan
            // no sabe que las plataformas colgantes se mueven con el peso y se
            // llega unos pixeles corrido. Al interruptor se le remata acercandose;
            // a la puerta hay que llegar de verdad.
            if (encargo[1] >= 0) {
                if (!asegurar(j, otro, meta, nombre(tipo))) return false;
            } else if (!Ruta.enPuerta(new int[] { j.x, j.y }, meta)) {
                System.out.println("  " + nombre(tipo) + ": acaba la ruta fuera de su puerta");
                return false;
            }
            System.out.println("  " + nombre(tipo) + " llega a "
                + (encargo[1] < 0 ? "su puerta" : "el interruptor '" + meta.tipo + "'")
                + " (" + tramo.size() + " movimientos)");
        }

        quietos(30);
        boolean gana = partida.hanGanado() && !partida.hayMuerto();
        System.out.println((gana ? "  OK   " : "  FALLA") + "  terminado en " + frames
            + " frames (" + frames / 60 + " s), albores " + partida.luz.recogidos
            + ", obsidianas " + partida.sombra.recogidos);
        return gana;
    }

    /** Grupos abiertos ahora mismo en la partida: palancas puestas y botones que pisa el otro. */
    static boolean[] abiertosDeVerdad(Jugador otro) {
        boolean[] abiertos = new boolean[Constantes.GRUPOS];
        for (Elemento e : interruptores) {
            if (e.grupo < 0 || e.grupo >= Constantes.GRUPOS) continue;
            if (Constantes.esPalanca(e.tipo) && e.encendida) abiertos[e.grupo] = true;
            if (Constantes.esBoton(e.tipo) && tocando(otro, e)) abiertos[e.grupo] = true;
        }
        return abiertos;
    }

    /** El boton cuenta solo si lo pisa **este** jugador: el otro se va a mover. */
    static boolean activado(Jugador j, Elemento e) {
        return Constantes.esPalanca(e.tipo) ? e.encendida : tocando(j, e);
    }

    static boolean tocando(Jugador j, Elemento e) {
        return j.getRectangulo(8).intersects(e.getRectangulo());
    }

    /**
     * Remata el encargo: la palanca ya se queda echada sola, pero al boton hay
     * que quedarse encima, y aterrizar puede dejar al personaje medio fuera. Se
     * separa y se acerca hasta dejarlo bien, igual que haria el jugador.
     */
    static boolean asegurar(Jugador j, Jugador otro, Elemento e, String quien) {
        int hacia = (j.x < e.x) ? 1 : -1;
        for (int intento = 0; intento < 6 && !activado(j, e); intento++) {
            for (int i = 0; i < 20 && tocando(j, e); i++) {
                mover(j, otro, -hacia);
            }
            for (int i = 0; i < 20 && !activado(j, e); i++) {
                mover(j, otro, hacia);
            }
        }
        j.velX = 0;
        quietos(5);
        if (!activado(j, e)) {
            System.out.println("  " + quien + ": no consigue dejar el interruptor activado");
            return false;
        }
        return true;
    }

    /** Ejecuta la ruta mientras el otro se queda quieto; para al cumplir la meta. */
    static boolean recorrer(Jugador j, Jugador otro, List<int[]> ruta, String quien,
                            java.util.function.BooleanSupplier meta) {
        int n = 0;
        for (int[] mov : ruta) {
            if (mov[1] == 1) j.saltar();
            for (int i = 0; ; i++) {
                j.velX = (i < mov[2]) ? mov[0] * Constantes.VEL_X : 0;
                otro.velX = 0;
                partida.actualizar();
                frames++;

                if (partida.hayMuerto()) {
                    System.out.println("  " + quien + ": muere en el movimiento " + n);
                    return false;
                }
                if (meta.getAsBoolean()) { aterrizar(j, otro); return true; }
                if (j.enSuelo && i >= 2) break;
                if (i > 300) {
                    System.out.println("  " + quien + ": se queda colgado en el movimiento " + n);
                    return false;
                }
            }
            n++;
        }
        return true;
    }

    static void aterrizar(Jugador j, Jugador otro) {
        for (int i = 0; i < 300 && !j.enSuelo; i++) {
            mover(j, otro, 0);
        }
    }

    static void mover(Jugador j, Jugador otro, int dir) {
        j.velX = dir * Constantes.VEL_X;
        otro.velX = 0;
        partida.actualizar();
        frames++;
    }

    static void quietos(int veces) {
        for (int i = 0; i < veces; i++) {
            partida.luz.velX = 0;
            partida.sombra.velX = 0;
            partida.actualizar();
            frames++;
        }
    }

    static Jugador jugador(int tipo) {
        return tipo == Jugador.LUZ ? partida.luz : partida.sombra;
    }

    static Jugador otro(Jugador j) {
        return j == partida.luz ? partida.sombra : partida.luz;
    }

    static String nombre(int tipo) {
        return tipo == Jugador.LUZ ? "Luz   " : "Sombra";
    }

    static Elemento buscar(char tipo) {
        for (Elemento e : nivel.elementos) if (e.tipo == tipo) return e;
        return null;
    }
}
