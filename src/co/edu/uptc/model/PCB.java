package co.edu.uptc.model;

/**
 * Clase PCB (Process Control Block)
 * ---------------------------------------------
 * Representa el bloque de control de proceso dentro del simulador.
 * Contiene la información administrativa básica de un proceso
 * y su tabla de páginas para la gestión de memoria.
 *
 * Invariantes:
 * - pageCount = ceil(logicalSizeBytes / pageSize)
 * - pageTable.length == pageCount
 */
public class PCB {

    /** Identificador único del proceso dentro de la simulación. */
    public final int pid;

    /** Estado actual del proceso (ciclo de vida: NEW, READY, RUNNING, etc.). */
    public ProcessState state;

    /** Tamaño lógico total del proceso en bytes. */
    public final int logicalSizeBytes;

    /** Número de páginas lógicas que ocupa el proceso en memoria. */
    public final int pageCount;

    /** Tabla de páginas del proceso, donde cada entrada corresponde a una página lógica. */
    public final PageTableEntry[] pageTable;

    /**
     * Constructor de PCB.
     * Inicializa un proceso con su PID, tamaño lógico y tabla de páginas vacía.
     * Cada entrada de la tabla de páginas se crea sin marco asignado inicialmente.
     *
     * @param pid              identificador único del proceso
     * @param logicalSizeBytes tamaño lógico del proceso en bytes
     * @param pageSize         tamaño de página en bytes (usado para calcular pageCount)
     */
    public PCB(int pid, int logicalSizeBytes, int pageSize) {
        this.pid = pid;
        this.logicalSizeBytes = logicalSizeBytes;
        // Calcular número de páginas con redondeo hacia arriba
        this.pageCount = (int) Math.ceil(logicalSizeBytes / (double) pageSize);
        // Crear tabla de páginas
        this.pageTable = new PageTableEntry[pageCount];
        for (int p = 0; p < pageCount; p++) {
            this.pageTable[p] = new PageTableEntry(p);
        }
        // Estado inicial siempre es NEW
        this.state = ProcessState.NEW;
    }
}
