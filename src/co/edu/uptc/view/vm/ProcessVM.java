package co.edu.uptc.view.vm;

/**
 * Clase ProcessVM (ViewModel / DTO)
 * -----------------------------------------------------------------
 * Objeto de transferencia de datos que representa un proceso
 * para ser mostrado en la vista, sin exponer directamente el modelo.
 *
 * Se utiliza en la tabla de procesos de la interfaz gráfica.
 */
public class ProcessVM {

    /** Identificador único del proceso (PID). */
    public final int pid;

    /**
     * Estado actual del proceso en forma de texto ("NEW", "READY", "RUNNING",
     * etc.).
     */
    public final String state;

    /** Tamaño lógico del proceso en bytes. */
    public final int sizeBytes;

    /** Número de páginas que ocupa el proceso en memoria. */
    public final int pages;

    /**
     * Constructor de ProcessVM.
     * Inicializa el DTO con la información que será mostrada en la interfaz.
     *
     * @param pid       identificador del proceso
     * @param state     estado actual en forma de texto
     * @param sizeBytes tamaño lógico en bytes
     * @param pages     número total de páginas
     */
    public ProcessVM(int pid, String state, int sizeBytes, int pages) {
        this.pid = pid;
        this.state = state;
        this.sizeBytes = sizeBytes;
        this.pages = pages;
    }
}
