package juego;

import java.io.File;
import java.io.InputStream;

import juego.utilidades.Assets;

/**
 * Dice donde esta buscando el juego sus archivos y si los encuentra.
 *
 * Es para cuando el juego arranca pero se ve sin imagenes y sin nivel: en vez
 * de adivinar, se lanza esto y se lee lo que sale. No abre ninguna ventana.
 */
public class Diagnostico {

    private static final String[] IMPRESCINDIBLES = {
        "assets/niveles/nivel1.txt",
        "assets/niveles/nivel4.txt",
        "assets/fondos/menus.png",
        "assets/fondos/nivel1.png",
        "assets/sprites/luz_quieto.png",
        "assets/sprites/sombra_quieto.png",
        "assets/sonidos/palanca.wav",
        "assets/sonidos/boton.wav",
        "assets/sonidos/albor.wav",
        "assets/sonidos/obsidiana.wav",
        "assets/sonidos/puerta.wav",
        "assets/sonidos/gameover.wav",
        "assets/sonidos/musica.wav",
    };

    public static void main(String[] args) {
        System.out.println("Luz y Sombra - diagnostico");
        System.out.println("==========================");
        System.out.println();
        System.out.println("Java                   " + System.getProperty("java.version")
                           + "   " + System.getProperty("java.vendor"));
        System.out.println("Sistema                " + System.getProperty("os.name")
                           + " " + System.getProperty("os.version")
                           + " (" + System.getProperty("os.arch") + ")");
        System.out.println("Directorio de trabajo  " + new File(".").getAbsolutePath());
        System.out.println("Codigo compilado en    " + dondeEstaElCodigo());
        System.out.println("Buscando assets en     " + Assets.donde());
        System.out.println("Encuentra assets       " + (Assets.hay() ? "SI" : "NO"));
        System.out.println();

        int faltan = 0;
        for (String ruta : IMPRESCINDIBLES) {
            if (!comprobar(ruta)) {
                faltan++;
            }
        }

        System.out.println();
        if (faltan == 0) {
            System.out.println("TODO EN SU SITIO. Si aun asi el juego se ve en blanco, el problema");
            System.out.println("no son los archivos: manda esta salida entera.");
        } else {
            System.out.println("FALTAN " + faltan + " ARCHIVO(S).");
            System.out.println("Copia la carpeta assets/ entera al lado de src/ y de bin/,");
            System.out.println("o usa el Luz-y-Sombra.jar, que ya la lleva dentro.");
        }
    }

    /** Intenta abrir el archivo de verdad y dice cuanto ocupa. */
    private static boolean comprobar(String ruta) {
        try (InputStream entrada = Assets.abrir(ruta)) {
            if (entrada == null) {
                System.out.println("  NO SE ABRE   " + ruta);
                System.out.println("               se probo en " + Assets.archivo(ruta).getAbsolutePath());
                return false;
            }
            System.out.println("  ok           " + ruta + "   (" + entrada.readAllBytes().length + " bytes)");
            return true;
        } catch (Exception e) {
            System.out.println("  ERROR        " + ruta + "   " + e);
            return false;
        }
    }

    private static String dondeEstaElCodigo() {
        try {
            return new File(Diagnostico.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (Exception e) {
            return "(no se puede saber)";
        }
    }

    private Diagnostico() {
    }
}
