import juego.controladores.ControladorNivel;
import juego.objetos.*;
import juego.utilidades.*;

public class PruebaMecanismos {
    static int fallos = 0;
    static final int C = Constantes.CELDA;
    static String ruta;

    static void ok(String n, boolean c, String d) {
        System.out.println((c ? "  OK   " : "  FALLA") + "  " + n + "   " + d);
        if (!c) fallos++;
    }
    static ControladorNivel nuevo() { return new ControladorNivel(CargadorNivel.cargar(ruta, "")); }
    /** El tablon cuyo simbolo es 'E' (sentidoBase +1) o 'F' (-1). */
    static Plataforma tablon(ControladorNivel n, int sentidoBase) {
        for (Plataforma p : n.nivel.plataformas) if (p.sentidoBase == sentidoBase) return p;
        return null;
    }
    static double grados(double rad) { return Math.round(Math.toDegrees(rad) * 10) / 10.0; }
    static void enSuelo(Jugador j, int col) {
        j.x = col*C + (C-j.ancho)/2; j.y = 23*C - j.alto; j.velX=0; j.velY=0;
    }
    static int[] muroDeGrupo(ControladorNivel n, int grupo) {
        for (int[] m : n.nivel.muros) if (m[2] == grupo) return m;
        return null;
    }
    /** Deja al jugador encima de esa columna unos cuantos frames. */
    static void pisa(ControladorNivel n, Jugador j, int col, int frames) {
        enSuelo(j, col);
        for (int i = 0; i < frames; i++) { j.x = col*C + 4; n.actualizar(); }
    }
    /** De pie sobre el tablon, con el centro en esa columna de pixeles. */
    static void encimaDe(ControladorNivel n, Jugador j, Plataforma p, int px, int frames) {
        for (int i = 0; i < frames; i++) {
            j.x = px - j.ancho / 2;
            j.y = p.alturaEn(px) - j.alto;
            j.velY = 0; j.velX = 0; j.enSuelo = true;
            n.actualizar();
        }
    }

    public static void main(String[] a) {
        ruta = a[0];
        ControladorNivel n = nuevo();
        System.out.println("plataformas=" + n.nivel.plataformas.size()
            + "  muros=" + n.nivel.muros.size() + "  elementos=" + n.nivel.elementos.size());

        Plataforma pe = tablon(n, 1), pf = tablon(n, -1);
        ok("hay un tablon de cada sentido", pe != null && pf != null, "");
        ok("cuelgan de su centro", pe.pivoteX == pe.x + pe.ancho / 2 && pe.pivoteY == pe.y,
           "pivote=(" + pe.pivoteX + "," + pe.pivoteY + ")");

        // --- sin nadie encima, ni se mueven ni se inclinan ---
        n = nuevo(); enSuelo(n.luz, 3); enSuelo(n.sombra, 7);
        for (int i=0;i<120;i++) n.actualizar();
        ok("colgadas y planas si nadie las pisa",
           tablon(n,1).angulo == 0 && tablon(n,-1).angulo == 0
           && tablon(n,1).y == pe.y && tablon(n,-1).y == pf.y, "");

        // --- con peso encima gira, y NO baja ---
        n = nuevo(); enSuelo(n.sombra, 7);
        encimaDe(n, n.luz, tablon(n,1), tablon(n,1).pivoteX, 40);
        ok("con peso encima se inclina", tablon(n,1).angulo > 0,
           grados(tablon(n,1).angulo) + " grados");
        ok("con peso encima NO baja", tablon(n,1).y == pe.y, "y=" + tablon(n,1).y);

        // --- cada tablon va por su cuenta ---
        // Es el fallo que tenia el juego: iban acoplados por grupo, asi que
        // pisar uno movia al otro aunque estuviera en la otra punta del mapa.
        ok("el otro tablon no se entera", tablon(n,-1).angulo == 0,
           grados(tablon(n,-1).angulo) + " grados");

        // --- cae hacia el lado por el que le tira el peso ---
        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma pd = tablon(n,1);
        encimaDe(n, n.luz, pd, pd.pivoteX + 50, 40);
        ok("con el peso a la derecha cae a la derecha", pd.angulo > 0,
           grados(pd.angulo) + " grados");

        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma pi = tablon(n,1);                    // el MISMO tablon 'E'
        encimaDe(n, n.luz, pi, pi.pivoteX - 50, 40);
        ok("y con el peso a la izquierda cae a la izquierda", pi.angulo < 0,
           grados(pi.angulo) + " grados");

        // --- pero no se equilibra: la inclinacion no se queda a medias ---
        // Con la posicion cambia hacia donde cae, no cuanto. Puesto en el borde
        // o pegado al pivote, a los 40 frames lleva la misma inclinacion.
        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma pa = tablon(n,1);
        encimaDe(n, n.luz, pa, pa.x + pa.ancho - 6, 40);
        double alBorde = pa.angulo;

        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma pp = tablon(n,1);
        encimaDe(n, n.luz, pp, pp.pivoteX + 1, 40);
        ok("no se equilibra: al borde se inclina igual que junto al pivote",
           alBorde == pp.angulo, grados(alBorde) + " vs " + grados(pp.angulo));

        // --- justo encima del pivote desempata el simbolo ---
        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma pz = tablon(n,-1);
        encimaDe(n, n.luz, pz, pz.pivoteX, 40);
        ok("en el pivote justo manda el simbolo ('F' a la izquierda)", pz.angulo < 0,
           grados(pz.angulo) + " grados");

        // --- con los dos repartidos tampoco se queda plana ---
        n = nuevo();
        Plataforma p2 = tablon(n,1);
        for (int i=0;i<40;i++) {
            int izq = p2.x + 20, der = p2.x + p2.ancho - 20;
            n.luz.x = izq - n.luz.ancho / 2;
            n.luz.y = p2.alturaEn(izq) - n.luz.alto;
            n.sombra.x = der - n.sombra.ancho / 2;
            n.sombra.y = p2.alturaEn(der) - n.sombra.alto;
            n.luz.velY = n.sombra.velY = 0; n.luz.enSuelo = n.sombra.enSuelo = true;
            n.actualizar();
        }
        ok("con los dos en lados opuestos sigue volcando", p2.angulo != 0,
           grados(p2.angulo) + " grados");

        // --- pasada la inclinacion de caida deja de sostener ---
        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma p3 = tablon(n,1);
        encimaDe(n, n.luz, p3, p3.pivoteX, 55);
        ok("a media inclinacion todavia sujeta", p3.sujeta(), grados(p3.angulo) + " grados");
        encimaDe(n, n.luz, p3, p3.pivoteX, 10);
        ok("pasada ANG_CAIDA ya no sujeta", !p3.sujeta(), grados(p3.angulo) + " grados");

        int antesDeCaer = n.luz.y;
        for (int i=0;i<10;i++) n.actualizar();          // sin sujetarlo a mano
        ok("y el personaje se cae", n.luz.y > antesDeCaer && !n.luz.enSuelo,
           "y " + antesDeCaer + " -> " + n.luz.y);

        // --- nunca se pasa de ANG_MAX ---
        n = nuevo(); enSuelo(n.sombra, 7);
        Plataforma p4 = tablon(n,1);
        encimaDe(n, n.luz, p4, p4.pivoteX, 400);
        ok("no se pasa de ANG_MAX", Math.abs(p4.angulo) <= Constantes.ANG_MAX,
           grados(p4.angulo) + " grados, tope " + grados(Constantes.ANG_MAX));

        // --- al bajarse vuelve a la horizontal ---
        n.luz.x = 3*C; n.luz.y = 23*C - n.luz.alto;
        for (int i=0;i<200;i++) n.actualizar();
        ok("vuelve a plana al quedarse sola", tablon(n,1).angulo == 0,
           grados(tablon(n,1).angulo) + " grados");

        // --- el boton quita el muro, y vuelve al soltarlo ---
        n = nuevo(); enSuelo(n.sombra, 7); enSuelo(n.luz, 3);
        n.actualizar();
        int[] m = n.nivel.muros.get(0);
        ok("el muro empieza puesto", n.nivel.mapa[m[0]][m[1]] == Constantes.BLOQUE, "");

        enSuelo(n.luz, 5);
        for (int i=0;i<10;i++) { n.luz.x = 5*C + 4; n.actualizar(); }
        ok("el boton quita el muro", n.nivel.mapa[m[0]][m[1]] == Constantes.VACIO, "");

        n.luz.x = 1*C;
        for (int i=0;i<10;i++) n.actualizar();
        ok("al soltar el boton vuelve el muro", n.nivel.mapa[m[0]][m[1]] == Constantes.BLOQUE, "");

        // --- la palanca no es una placa: mantiene lo que ha hecho ---
        n = nuevo(); enSuelo(n.sombra, 30); enSuelo(n.luz, 3);
        n.actualizar();
        int[] mp = muroDeGrupo(n, 1);
        ok("el muro de la palanca empieza puesto", n.nivel.mapa[mp[0]][mp[1]] == Constantes.BLOQUE, "");

        pisa(n, n.luz, 10, 10);
        ok("la palanca quita el muro", n.nivel.mapa[mp[0]][mp[1]] == Constantes.VACIO, "");

        n.luz.x = 1*C;
        for (int i=0;i<10;i++) n.actualizar();
        ok("la palanca se queda echada al irse", n.nivel.mapa[mp[0]][mp[1]] == Constantes.VACIO, "");

        pisa(n, n.luz, 10, 10);
        n.luz.x = 1*C;
        for (int i=0;i<10;i++) n.actualizar();
        ok("volver a tocarla no la deshace", n.nivel.mapa[mp[0]][mp[1]] == Constantes.VACIO, "");

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASAN" : "\n" + fallos + " PRUEBA(S) FALLAN");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
