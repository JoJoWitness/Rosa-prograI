package juego.objetos;

import java.util.ArrayList;

public class Nivel {

    public char[][] mapa;
    public ArrayList<Elemento> elementos;
    public ArrayList<Plataforma> plataformas;
    /** Celdas de muro movil: {fila, columna, grupo}. */
    public ArrayList<int[]> muros;
    /** Muros que cruzan el pilar: {fila, colReposo, colCruzada, grupo}. */
    public ArrayList<int[]> murosCruzan;

    public int filas;
    public int columnas;

    public int luzX;
    public int luzY;
    public int sombraX;
    public int sombraY;

    public int totalAlbores;
    public int totalObsidianas;

    public String ruta;
    public String rutaFondo;

    public Nivel(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        this.mapa = new char[filas][columnas];
        this.elementos = new ArrayList<Elemento>();
        this.plataformas = new ArrayList<Plataforma>();
        this.muros = new ArrayList<int[]>();
        this.murosCruzan = new ArrayList<int[]>();
    }
}
