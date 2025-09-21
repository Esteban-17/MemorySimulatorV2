package co.edu.uptc.view.vm;

/**
 * Clase FrameVM (ViewModel / DTO)
 * -----------------------------------------------------------------
 * Objeto de transferencia de datos que representa un marco físico
 * para ser mostrado en la vista, sin exponer directamente el modelo.
 *
 * Se utiliza en el patrón MVP para aislar la vista de la lógica interna.
 */
public class FrameVM {

    /** Número del marco físico dentro de la memoria. */
    public final int frameNumber;

    /** Indica si el marco está libre (true) o en uso (false). */
    public final boolean free;

    /** Texto con el PID del proceso que ocupa el marco, o "-" si está libre. */
    public final String pidText;

    /** Texto con el número de página asignada, o "-" si está libre. */
    public final String pageText;

    /**
     * Constructor de FrameVM.
     * Inicializa el DTO con los datos que serán mostrados en la interfaz.
     *
     * @param frameNumber número del marco físico
     * @param free        indica si está libre
     * @param pidText     texto con el PID del proceso (o "-")
     * @param pageText    texto con el número de página (o "-")
     */
    public FrameVM(int frameNumber, boolean free, String pidText, String pageText) {
        this.frameNumber = frameNumber;
        this.free = free;
        this.pidText = pidText;
        this.pageText = pageText;
    }
}
