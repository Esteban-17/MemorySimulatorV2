package co.edu.uptc.model;

/**
 * Clase Frame
 * ----------------------------
 * Representa un marco físico dentro de la memoria principal.
 * Cada marco puede estar libre o bien ocupado por una página de algún proceso.
 * Sirve como unidad básica de asignación en un esquema de paginación.
 */
public class Frame {

    /** Índice del marco físico (0..N-1), identifica de manera única este marco. */
    public final int frameNumber;

    /** Indica si el marco está disponible (true) o asignado a un proceso (false). */
    public boolean free = true;

    /** PID del proceso que ocupa este marco; es null si el marco está libre. */
    public Integer pid = null;

    /** Número de página del proceso que se encuentra almacenada en este marco; null si está libre. */
    public Integer pageNumber = null;

    /**
     * Constructor de Frame.
     * Crea un marco físico inicializado como libre, identificado por su número.
     * @param frameNumber índice único del marco físico
     */
    public Frame(int frameNumber) {
        this.frameNumber = frameNumber;
    }
}
