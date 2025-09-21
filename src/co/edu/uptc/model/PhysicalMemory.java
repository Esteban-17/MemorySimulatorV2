package co.edu.uptc.model;

/**
 * Clase PhysicalMemory
 * ------------------------------------------
 * Representa la memoria física del sistema simulador.
 * Está compuesta por un conjunto de marcos (frames) que pueden
 * ser asignados a las páginas de los procesos.
 *
 * Esta clase también guarda el tamaño de página (pageSize),
 * que se usa para traducir direcciones lógicas y para visualización.
 */
public class PhysicalMemory {

    /** Tamaño de página en bytes (ejemplo: 1024). */
    public final int pageSize;

    /** Arreglo de marcos físicos que conforman la memoria principal. */
    public final Frame[] frames;

    /**
     * Constructor de PhysicalMemory.
     * Inicializa la memoria física creando N marcos vacíos.
     *
     * @param pageSize  tamaño de cada página en bytes (ejemplo: 1024)
     * @param numFrames número total de marcos físicos disponibles (ejemplo: 64)
     */
    public PhysicalMemory(int pageSize, int numFrames) {
        this.pageSize = pageSize;
        this.frames = new Frame[numFrames];
        // Crear todos los marcos e inicializarlos como libres
        for (int i = 0; i < numFrames; i++) {
            this.frames[i] = new Frame(i);
        }
    }
}
