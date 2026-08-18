package juego.utilidades;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Encuentra la carpeta assets/ y reparte los archivos que hay dentro.
 *
 * Todas las rutas del juego se escriben relativas ("assets/fondos/menus.png"),
 * y una ruta relativa se resuelve contra el directorio de trabajo. Ese
 * directorio no siempre es la carpeta del proyecto: al abrir el juego con doble
 * clic, o desde un IDE, o desde otra carpeta, es otro y no se encuentra nada.
 * Aqui se busca la carpeta de verdad una sola vez.
 */
public class Assets {

    private static File base;

    /** El archivo que toca, este donde este la carpeta del proyecto. */
    public static File archivo(String ruta) {
        return limpia(new File(base(), ruta));
    }

    /** Quita el "\.\" de en medio, para que los mensajes se puedan leer. */
    private static File limpia(File f) {
        try {
            return f.getCanonicalFile();
        } catch (Exception e) {
            return f.getAbsoluteFile();
        }
    }

    /**
     * Abre un recurso para leerlo: primero el archivo suelto de la carpeta
     * assets/, y si no esta, la copia que viaja dentro del jar. Devuelve null
     * si no hay ninguna de las dos.
     *
     * Ese orden es a proposito: con el jar hecho se siguen pudiendo cambiar los
     * niveles y las imagenes dejando una carpeta assets/ al lado, sin volver a
     * empaquetar nada.
     */
    public static InputStream abrir(String ruta) {
        File suelto = archivo(ruta);
        if (suelto.isFile()) {
            try {
                return new FileInputStream(suelto);
            } catch (Exception e) {
                // ilegible: se prueba con la copia de dentro del jar
            }
        }
        return Assets.class.getClassLoader().getResourceAsStream(ruta);
    }

    /** La carpeta desde la que se estan leyendo los archivos, para poder decirlo. */
    public static String donde() {
        return limpia(base()).getPath();
    }

    /** true si se encontro assets/; false si de verdad no esta. */
    public static boolean hay() {
        return new File(base(), "assets").isDirectory()
            || Assets.class.getClassLoader().getResource("assets") != null;
    }

    private static File base() {
        if (base != null) {
            return base;
        }

        // 1. El directorio de trabajo, que es lo normal con los guiones.
        if (new File("assets").isDirectory()) {
            base = new File(".");
            return base;
        }

        // 2. Subiendo desde donde esta el codigo compilado: bin/ cuelga de la
        //    carpeta del proyecto, y assets/ es su hermana.
        base = buscarJuntoAlCodigo();
        if (base != null) {
            return base;
        }

        // 3. No hay carpeta suelta. Si el juego va empaquetado no pasa nada,
        //    porque los recursos viajan dentro del jar; si tampoco estan ahi,
        //    se avisa una vez y se sigue con el nivel de reserva.
        base = new File(".");
        if (Assets.class.getClassLoader().getResource("assets") == null) {
            System.out.println("AVISO: no encuentro la carpeta assets/, asi que el juego se ve");
            System.out.println("       sin imagenes y sin niveles. La espero aqui:");
            System.out.println();
            System.out.println("           " + limpia(new File(base, "assets")).getPath());
            System.out.println();
            System.out.println("       Copia ahi la carpeta assets/ entera, o usa el");
            System.out.println("       Luz-y-Sombra.jar, que ya la lleva dentro.");
        }
        return base;
    }

    private static File buscarJuntoAlCodigo() {
        try {
            java.security.CodeSource origen =
                Assets.class.getProtectionDomain().getCodeSource();
            if (origen == null) {
                return null;
            }

            File carpeta = new File(origen.getLocation().toURI());
            if (carpeta.isFile()) {          // si es un .jar, la carpeta es la suya
                carpeta = carpeta.getParentFile();
            }
            for (int i = 0; i < 5 && carpeta != null; i++) {
                if (new File(carpeta, "assets").isDirectory()) {
                    return carpeta;
                }
                carpeta = carpeta.getParentFile();
            }
        } catch (Exception e) {
            // ruta rara o sin permisos: se sigue con el directorio de trabajo
        }
        return null;
    }

    private Assets() {
    }
}
