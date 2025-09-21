package co.edu.uptc.model;

/**
 * Clase PageTableEntry
 * --------------------------------
 * Representa una entrada individual dentro de la tabla de páginas de un proceso.
 * Cada entrada define cómo una página lógica específica se relaciona con un
 * marco físico en la memoria principal, además de incluir bits de control
 * usados en la administración de memoria virtual.
 */
public class PageTableEntry {

    /** Número de página lógica dentro del proceso (0..pageCount-1). */
    public final int pageNumber;

    /** Número de marco físico al que está asignada esta página; null si no está cargada. */
    public Integer frameNumber;

    /** Bit de presencia: indica si la página se encuentra cargada en un marco físico. */
    public boolean present;

    /** Bit de referencia: indica si la página ha sido accedida recientemente. */
    public boolean referenced;

    /** Bit de modificado: indica si la página ha sido escrita (dirty bit). */
    public boolean dirty;

    /**
     * Constructor de PageTableEntry.
     * Crea una entrada inicialmente sin marco asignado y con todos los bits en false.
     * 
     * @param pageNumber índice de la página lógica que representa esta entrada
     */
    public PageTableEntry(int pageNumber) {
        this.pageNumber = pageNumber;
        this.frameNumber = null;
        this.present = false;
        this.referenced = false;
        this.dirty = false;
    }
}
