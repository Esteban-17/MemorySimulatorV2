package co.edu.uptc.model;

/**
 * Enum ProcessState
 * ------------------------------------------
 * Define los 7 estados posibles en el ciclo de vida de un proceso
 * dentro del simulador. 
 *
 * NOTA: El cambio entre estos estados no lo maneja el modelo directamente,
 * sino el Presenter, que se encarga de controlar las transiciones.
 */
public enum ProcessState {

    /** Estado NEW: proceso recién creado, aún no admitido en memoria. */
    NEW,

    /** Estado READY: proceso listo para ejecutar; ya tiene marcos asignados (si aplica). */
    READY,

    /** Estado RUNNING: proceso en ejecución (no cambia ocupación de marcos). */
    RUNNING,

    /** Estado WAITING: proceso bloqueado o esperando E/S; conserva sus marcos si estaba admitido. */
    WAITING,

    /** Estado SUSP_READY: proceso suspendido y listo; en este simulador puede liberar marcos. */
    SUSP_READY,

    /** Estado SUSP_BLOCKED: proceso suspendido y bloqueado. */
    SUSP_BLOCKED,

    /** Estado TERMINATED: proceso finalizado; debe liberar sus marcos y limpiar su tabla de páginas. */
    TERMINATED
}
