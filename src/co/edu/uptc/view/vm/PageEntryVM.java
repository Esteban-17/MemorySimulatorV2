package co.edu.uptc.view.vm;

/**
 * Clase PageEntryVM (ViewModel / DTO)
 * -----------------------------------------------------------------
 * Objeto de transferencia de datos que representa una fila de la
 * tabla de páginas de un proceso, lista para mostrarse en la vista.
 *
 * Se usa en el patrón MVP para desacoplar la vista del modelo real.
 */
public class PageEntryVM {

    /** Número de página lógica dentro del proceso. */
    public final int page;

    /** Indica si la página está presente en memoria (true) o no (false). */
    public final boolean present;

    /** Texto con el número de marco asignado, o "-" si no está cargada. */
    public final String frameText;

    /** Bit de referencia: true si la página fue accedida recientemente. */
    public final boolean referenced;

    /** Bit de modificado: true si la página fue escrita en memoria. */
    public final boolean dirty;

    /**
     * Constructor de PageEntryVM.
     * Inicializa los datos que se mostrarán en la interfaz de la tabla de páginas.
     *
     * @param page       número de página lógica
     * @param present    indica si la página está en memoria
     * @param frameText  texto con el marco asignado (o "-")
     * @param referenced bit de referencia
     * @param dirty      bit de modificado
     */
    public PageEntryVM(int page, boolean present, String frameText, boolean referenced, boolean dirty) {
        this.page = page;
        this.present = present;
        this.frameText = frameText;
        this.referenced = referenced;
        this.dirty = dirty;
    }
}
