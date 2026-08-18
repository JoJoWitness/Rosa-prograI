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
    static Plataforma queBaja(ControladorNivel n) {
        for (Plataforma p : n.nivel.plataformas) if (p.baja) return p;
        return null;
    }
    static Plataforma contrapeso(ControladorNivel n) {
        for (Plataforma p : n.nivel.plataformas) if (!p.baja) return p;
        return null;
    }
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
    /** Deja al jugador de pie sobre la plataforma y corre frames. */
    static void encimaDe(ControladorNivel n, Jugador j, Plataforma p, int frames) {
        for (int i = 0; i < frames; i++) {
            j.x = p.x + 10;
            j.y = p.y - j.alto;
            j.velY = 0; j.velX = 0; j.enSuelo = true;
            n.actualizar();
        }
    }

    public static void main(String[] a) {
        ruta = a[0];
        ControladorNivel n = nuevo();
        System.out.println("plataformas=" + n.nivel.plataformas.size()
            + "  muros=" + n.nivel.muros.size() + "  elementos=" + n.nivel.elementos.size());

        Plataforma pb = queBaja(n), pc = contrapeso(n);
        ok("la plataforma con peso baja", pb.yActiva > pb.yReposo,
           "reposo=" + pb.yReposo + " activa=" + pb.yActiva);
        ok("el contrapeso sube", pc.yActiva < pc.yReposo,
           "reposo=" + pc.yReposo + " activa=" + pc.yActiva);

        // --- sin nadie encima, no se mueven ---
        n = nuevo(); enSuelo(n.luz, 3); enSuelo(n.sombra, 7);
        for (int i=0;i<120;i++) n.actualizar();
        ok("colgadas quietas si nadie las pisa", queBaja(n).y == pb.yReposo
            && contrapeso(n).y == pc.yReposo, "y=" + queBaja(n).y);

        // --- con peso encima baja, y el contrapeso sube ---
        n = nuevo(); enSuelo(n.sombra, 7);
        encimaDe(n, n.luz, queBaja(n), 120);
        ok("baja mientras alguien esta encima", queBaja(n).y == pb.yActiva,
           "y=" + queBaja(n).y + " esperado=" + pb.yActiva);
        ok("el contrapeso sube a la vez", contrapeso(n).y == pc.yActiva,
           "y=" + contrapeso(n).y + " esperado=" + pc.yActiva);

        // --- al bajarse, vuelve a su sitio ---
        n.luz.x = 3*C; n.luz.y = 23*C - n.luz.alto;
        for (int i=0;i<200;i++) n.actualizar();
        ok("vuelve a colgar al quedarse sola", queBaja(n).y == pb.yReposo,
           "y=" + queBaja(n).y);

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
