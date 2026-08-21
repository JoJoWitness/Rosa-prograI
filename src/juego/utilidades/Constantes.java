package juego.utilidades;

import java.awt.Color;

public class Constantes {

    public static final int ANCHO = 1920;
    public static final int ALTO = 1080;

    // 41 y no 43: la rejilla pasa a 26 filas y 26 * 43 no cabe en los 1080 px de
    // alto. Manda el numero de filas; la celda se ajusta a lo que quepa.
    public static final int CELDA = 41;
    public static final int COLUMNAS = 43;
    // 26 y no 22: dos filas extra de alto respecto a la maquetacion, para que el
    // salto no se corte contra el techo en los pisos de arriba. Las dos van
    // debajo del muro superior, asi que el suelo y la estructura no se mueven.
    public static final int FILAS = 26;

    public static final int MARGEN_X = (ANCHO - COLUMNAS * CELDA) / 2;
    public static final int MARGEN_Y = (ALTO - FILAS * CELDA) / 2;

    // La cuadricula dibujada en las maquetaciones mide 44 px y su area interior
    // empieza en (48, 44). Manda la rejilla del juego, asi que el fondo se
    // escala para que su marco caiga sobre la celda 1.
    public static final int CELDA_MAQUETA = 44;
    public static final int ORIGEN_MAQUETA_X = 48;
    public static final int ORIGEN_MAQUETA_Y = 44;
    public static final int COLUMNAS_MAQUETA = 41;   // area interior de la maqueta
    public static final int FILAS_MAQUETA = 22;

    public static final int MS_POR_FRAME = 16;

    public static final int GRAVEDAD = 1;
    public static final int VEL_MAX_CAIDA = 20;
    public static final int VEL_X = 7;
    // Sube 231 px, unas 5,6 celdas. Los escalones de los niveles miden entre 4 y
    // 6 celdas: con menos no se alcanza la plataforma de al lado. Los de 6 o mas
    // se suben con las plataformas moviles.
    public static final int FUERZA_SALTO = -22;

    // Alto: 3 celdas exactas. Ancho: menos de una celda, para no atascarse en
    // pasillos estrechos por errores de un pixel.
    public static final int ANCHO_JUGADOR = 34;
    public static final int ALTO_JUGADOR = 3 * CELDA;

    public static final char VACIO = '.';
    public static final char BLOQUE = '#';
    public static final char PUAS = '^';
    public static final char INICIO_LUZ = '1';
    public static final char INICIO_SOMBRA = '2';
    public static final char LUMINOSIDAD = 'A';
    public static final char PENUMBRA = 'B';
    public static final char ALBOR = 'o';
    public static final char OBSIDIANA = 'x';
    public static final char PUERTA_LUZ = 'P';
    public static final char PUERTA_SOMBRA = 'Q';

    // Interruptores. Cada uno pertenece a un grupo y manda sobre los muros de
    // ese grupo, lo que permite cruzarlos (la palanca de la derecha abre el
    // muro de la izquierda y viceversa). Las plataformas no llevan grupo.
    public static final char BOTON = 'b';           // grupo 0
    public static final char BOTON_2 = 'c';         // grupo 1
    public static final char PALANCA = 'v';         // grupo 0
    public static final char PALANCA_2 = 'w';       // grupo 1
    // Las plataformas NO las mueven los botones y NO bajan: giran. Cuelgan de
    // su centro, asi que el peso solo puede inclinarlas. Cada tablon va por su
    // cuenta: no hay grupos ni parejas, solo cuenta quien esta encima de el.
    public static final char PLATAFORMA = 'E';      // gira a la derecha
    public static final char PLATAFORMA_2 = 'F';    // gira a la izquierda

    // Muro de ladrillos que se quita mientras su grupo esta activado.
    public static final char MURO = 'M';            // grupo 0
    public static final char MURO_2 = 'N';          // grupo 1
    // Muro doble: no basta con un grupo, hacen falta los dos a la vez.
    public static final char MURO_AMBOS = 'D';
    // Muro que CRUZA: no se quita, se pasa al otro lado del pilar del que sale.
    // Es una repisa de una celda que va y viene, y como el pilar la parte por
    // la mitad, sirve de escalon a un pozo o al otro, nunca a los dos.
    public static final char MURO_CRUZA = 'm';      // grupo 0, oscuro
    public static final char MURO_CRUZA_2 = 'n';    // grupo 1, claro

    public static final int GRUPOS = 2;

    /**
     * El paso a la sala de las puertas no pertenece a ningun grupo: lo abre
     * cualquier placa pisada, y solo mientras se pisa. Lleva su propia ranura
     * al final del array, detras de los grupos de verdad.
     */
    public static final int GRUPO_AMBOS = GRUPOS;

    /** Tamano del array de grupos: los GRUPOS de verdad mas la ranura del paso. */
    public static final int RANURAS = GRUPOS + 1;

    public static int grupoDe(char simbolo) {
        switch (simbolo) {
            case BOTON: case PALANCA: case MURO: case MURO_CRUZA:
                return 0;
            case BOTON_2: case PALANCA_2: case MURO_2: case MURO_CRUZA_2:
                return 1;
            case MURO_AMBOS:
                return GRUPO_AMBOS;
            default:
                return -1;
        }
    }

    public static boolean esBoton(char c)   { return c == BOTON || c == BOTON_2; }
    public static boolean esPalanca(char c) { return c == PALANCA || c == PALANCA_2; }
    public static boolean esPlataforma(char c) {
        // 'e' y 'f' eran el contrapeso de la polea, que ya no existe. Se
        // aceptan como alias para que un .txt antiguo siga cargando.
        return c == PLATAFORMA || c == PLATAFORMA_2 || c == 'e' || c == 'f';
    }
    /** Hacia donde gira ese tablon: +1 a la derecha, -1 a la izquierda. */
    public static int sentidoDe(char c) {
        return (c == PLATAFORMA || c == 'e') ? 1 : -1;
    }

    public static boolean esMuro(char c) { return c == MURO || c == MURO_2 || c == MURO_AMBOS; }
    public static boolean esMuroQueCruza(char c) { return c == MURO_CRUZA || c == MURO_CRUZA_2; }

    /** true si al muro de ese grupo le toca estar quitado. */
    public static boolean muroQuitado(int grupo, boolean[] abiertos) {
        return grupo >= 0 && grupo < abiertos.length && abiertos[grupo];
    }

    public static final int ALTO_PLATAFORMA = 28;
    // Tope de inclinacion, 34 grados. Lo que se llega a ver de pie es hasta
    // ANG_CAIDA (26 grados), que es donde el tablon deja de sostener: la punta
    // de uno de 4 celdas baja para entonces 40 px, casi una celda entera.
    public static final double ANG_MAX = 0.60;
    public static final double ANG_CAIDA = 0.45;
    // Radianes por frame. Se mantienen los 60 frames de pie hasta que suelta
    // (0,45 / 0,0075): cruzar el tablon mas largo del juego, el de 7 celdas del
    // nivel 4, cuesta 41 frames, asi que el margen no cambia al inclinarse mas.
    public static final double VEL_GIRO = 0.0075;
    // Cuanto se le perdona a los pies para seguir contando como apoyados en el
    // tablon. Andando sobre el ya inclinado, la superficie se mueve bajo los
    // pies VEL_X * tan(ANG_CAIDA) = 3,4 px por frame, mas 0,8 del giro. Sin
    // margen se pierde el apoyo en un frame, y como el tablon es de una sola
    // cara ya no vuelve a coger: cuesta arriba te hundes y cuesta abajo flotas,
    // y en los dos casos se cuela de largo.
    public static final int PEGADO_TABLON = 8;

    public static final Color COLOR_FONDO = new Color(172, 148, 123);
    public static final Color COLOR_MARCO = new Color(56, 48, 40);
    public static final Color COLOR_BLOQUE = new Color(57, 49, 41);
    public static final Color COLOR_REJILLA = new Color(120, 110, 100, 60);
    public static final Color COLOR_PUAS = new Color(20, 18, 16);
    public static final Color COLOR_LUZ = new Color(245, 242, 235);
    public static final Color COLOR_SOMBRA = new Color(25, 22, 20);
    public static final Color COLOR_LUMINOSIDAD = new Color(235, 232, 225);
    public static final Color COLOR_PENUMBRA = new Color(15, 13, 12);

    private Constantes() {
    }
}
