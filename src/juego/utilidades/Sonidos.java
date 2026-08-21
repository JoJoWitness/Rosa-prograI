package juego.utilidades;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Los efectos de sonido del juego.
 *
 * Son WAV y no MP3 a proposito: javax.sound.sampled, que es lo que trae Java de
 * serie, solo lee PCM (wav, au, aiff). Para el MP3 haria falta una biblioteca de
 * fuera, y el proyecto no lleva dependencias externas.
 *
 * El sonido es un adorno: si la maquina no tiene tarjeta, o el archivo falta, o
 * se lanza con -DsinSonido, el juego sigue igual y en silencio. Por eso aqui
 * todo se traga las excepciones en vez de propagarlas.
 */
public class Sonidos {

    public static final String PALANCA = "assets/sonidos/palanca.wav";
    public static final String BOTON = "assets/sonidos/boton.wav";
    public static final String ALBOR = "assets/sonidos/albor.wav";
    public static final String OBSIDIANA = "assets/sonidos/obsidiana.wav";
    public static final String PUERTA = "assets/sonidos/puerta.wav";
    public static final String GAMEOVER = "assets/sonidos/gameover.wav";
    public static final String MUSICA = "assets/sonidos/musica.wav";

    // Decibelios que se le restan a la musica respecto al nivel del archivo.
    // -18 dB deja la amplitud en un 12%: se oye de fondo y no tapa ni las
    // palancas ni las placas, que suenan a volumen entero.
    private static final float VOLUMEN_MUSICA = -18f;

    private static Clip musica;

    private static final HashMap<String, Clip> cache = new HashMap<String, Clip>();
    private static boolean mudo = System.getProperty("sinSonido") != null;

    /**
     * Suena el efecto desde el principio. Si ya estaba sonando, vuelve a
     * empezar: un Clip no se puede solapar consigo mismo, y para un efecto
     * corto reengancharlo suena mejor que dejarlo a medias o no hacer nada.
     */
    public static void reproducir(String ruta) {
        if (mudo) {
            return;
        }

        Clip clip = clip(ruta);
        if (clip == null) {
            return;
        }

        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Arranca la musica de fondo en bucle, bajita. Llamarla otra vez no la
     * reinicia: la partida se puede reiniciar sin que la musica salte.
     */
    public static void musicaEnBucle() {
        if (mudo || musica != null) {
            return;
        }

        musica = clip(MUSICA);
        if (musica == null) {
            return;
        }

        bajarVolumen(musica, VOLUMEN_MUSICA);
        musica.setFramePosition(0);
        musica.loop(Clip.LOOP_CONTINUOUSLY);   // sin fin y sin corte entre vueltas
    }

    public static void pararMusica() {
        if (musica != null) {
            musica.stop();
        }
    }

    // No todos los mezcladores dejan tocar el volumen. Si este no deja, la
    // musica suena al nivel del archivo en vez de quedarse el juego mudo.
    private static void bajarVolumen(Clip clip, float dB) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            System.out.println("Este equipo no deja ajustar el volumen de la musica.");
            return;
        }
        FloatControl v = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        v.setValue(Math.max(v.getMinimum(), Math.min(v.getMaximum(), dB)));
    }

    /** Deja el juego en silencio para siempre; lo usan las pruebas. */
    public static void silenciar() {
        mudo = true;
        pararMusica();
    }

    // Se abre una vez por archivo y se queda abierto: abrir un Clip cuesta
    // milisegundos, y un efecto que llega tarde no sirve de nada.
    private static Clip clip(String ruta) {
        if (cache.containsKey(ruta)) {
            return cache.get(ruta);
        }

        Clip clip = null;
        try (InputStream entrada = Assets.abrir(ruta)) {
            if (entrada == null) {
                System.out.println("Falta el sonido " + Assets.archivo(ruta).getAbsolutePath());
            } else {
                // AudioSystem necesita poder rebobinar la cabecera del wav, y
                // el flujo del jar no siempre deja: se envuelve en uno que si.
                AudioInputStream audio =
                    AudioSystem.getAudioInputStream(new BufferedInputStream(entrada));
                clip = AudioSystem.getClip();
                clip.open(audio);
            }
        } catch (Exception e) {
            // Sin tarjeta de sonido, o formato que no entiende: silencio y ya.
            System.out.println("No se pudo abrir el sonido " + ruta + ", se sigue sin el.");
            mudo = true;
        }

        cache.put(ruta, clip);
        return clip;
    }

    private Sonidos() {
    }
}
