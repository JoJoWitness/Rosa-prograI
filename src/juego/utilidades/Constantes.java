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

    // Mecanismos. Cada uno pertenece a un grupo: un boton solo manda sobre las
    // plataformas de su grupo, lo que permite cruzarlos (el boton de la derecha
    // mueve la plataforma de la izquierda y viceversa).
    public static final char BOTON = 'b';           // grupo 0
    public static final char BOTON_2 = 'c';         // grupo 1
    public static final char PALANCA = 'v';         // grupo 0
    public static final char PALANCA_2 = 'w';       // grupo 1
    // Las plataformas NO las mueven los botones: las mueve el peso. Cuelgan en
    // el aire y bajan mientras alguien esta encima, como en Fireboy & Watergirl.
    public static final char PLATAFORMA = 'E';      // grupo 0, baja con peso
    public static final char PLATAFORMA_INV = 'e';  // grupo 0, contrapeso: sube
    public static final char PLATAFORMA_2 = 'F';    // grupo 1, baja con peso
    public static final char PLATAFORMA_2_INV = 'f';// grupo 1, contrapeso: sube

    // Muro de ladrillos que se quita mientras su grupo esta activado.
    public static final char MURO = 'M';            // grupo 0
    public static final char MURO_2 = 'N';          // grupo 1
    // Muro doble: no basta con un grupo, hacen falta los dos a la vez.
    public static final char MURO_AMBOS = 'D';

    public static final int GRUPOS = 2;

    /** Grupo ficticio del muro doble: no es un grupo, es "todos a la vez". */
    public static final int GRUPO_AMBOS = GRUPOS;

    public static int grupoDe(char simbolo) {
        switch (simbolo) {
            case BOTON: case PALANCA: case PLATAFORMA: case PLATAFORMA_INV: case MURO:
                return 0;
            case BOTON_2: case PALANCA_2: case PLATAFORMA_2: case PLATAFORMA_2_INV: case MURO_2:
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
        return c == PLATAFORMA || c == PLATAFORMA_INV
            || c == PLATAFORMA_2 || c == PLATAFORMA_2_INV;
    }
    /** true si la plataforma baja al haber peso; false si es el contrapeso. */
    public static boolean plataformaBaja(char c) {
        return c == PLATAFORMA || c == PLATAFORMA_2;
    }

    public static boolean esMuro(char c) { return c == MURO || c == MURO_2 || c == MURO_AMBOS; }

    /** true si al muro de ese grupo le toca estar quitado. */
    public static boolean muroQuitado(int grupo, boolean[] abiertos) {
        if (grupo == GRUPO_AMBOS) {
            for (boolean a : abiertos) {
                if (!a) {
                    return false;
                }
            }
            return true;
        }
        return grupo >= 0 && grupo < abiertos.length && abiertos[grupo];
    }

    public static final int ALTO_PLATAFORMA = 28;
    public static final int RECORRIDO_PLATAFORMA = 2 * CELDA;
    public static final int VEL_PLATAFORMA = 3;

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
