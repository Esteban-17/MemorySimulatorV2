package co.edu.uptc.model;

/**
 * Interfaz MemoryManager
 * --------------------------------
 * Define el contrato que debe cumplir cualquier gestor de memoria
 * dentro del simulador. 
 *
 * Su responsabilidad es administrar la asignación y liberación de marcos
 * a los procesos (PCB) y permitir el mapeo de direcciones lógicas a físicas.
 *
 * Política recomendada (simple):
 * - Asignación: usar first-fit para asignar marcos libres a todas las páginas del proceso.
 * - Liberación: devolver todos los marcos al conjunto de marcos libres y limpiar la tabla de páginas.
 * - Mapeo: traducir dirección lógica en base a (página, desplazamiento) → marco físico.
 */
public interface MemoryManager {

    /**
     * Admite un proceso en memoria.
     * Asigna un marco por cada página lógica del proceso.
     * 
     * Reglas:
     * - Si existen marcos suficientes para todas las páginas, se asignan.
     * - Si no hay memoria suficiente, se retorna false y no se asigna nada.
     *
     * @param pcb proceso que se intenta admitir
     * @return true si se asignaron todos los marcos, false si no había suficientes
     */
    boolean admitProcess(PCB pcb);

    /**
     * Libera todos los marcos usados por un proceso.
     * Además, reinicia las entradas de su tabla de páginas,
     * dejando al proceso sin páginas presentes en memoria.
     *
     * @param pcb proceso a liberar
     */
    void releaseProcess(PCB pcb);

    /**
     * Traduce una dirección lógica a una dirección física.
     * Pasos:
     * - Calcular número de página: addr / pageSize
     * - Calcular desplazamiento dentro de la página: addr % pageSize
     * - Verificar que la página está en rango y presente
     * - Obtener marco físico y componer dirección física: frame*pageSize + offset
     *
     * @param pcb proceso al que pertenece la dirección lógica
     * @param logicalAddr dirección lógica en bytes (>= 0)
     * @return dirección física absoluta
     * @throws IllegalArgumentException si la página está fuera de rango o no está presente
     */
    int mapLogicalToPhysical(PCB pcb, int logicalAddr) throws IllegalArgumentException;
}
