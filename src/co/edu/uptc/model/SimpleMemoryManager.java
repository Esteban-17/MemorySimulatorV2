package co.edu.uptc.model;

/**
 * Clase SimpleMemoryManager
 * ----------------------------------------
 * Implementación concreta de la interfaz MemoryManager.
 * 
 * Política de asignación:
 * - Usa FIRST-FIT (todo-o-nada).
 * * Si hay suficientes marcos libres para todas las páginas del proceso,
 * se asignan los primeros que se encuentren.
 * * Si no hay suficientes marcos, no se asigna nada.
 * 
 * Responsabilidades:
 * - Admitir procesos: asignando marcos y actualizando su tabla de páginas.
 * - Liberar procesos: devolver todos los marcos ocupados y limpiar la tabla de
 * páginas.
 * - Mapear direcciones: traducir direcciones lógicas a físicas validando rangos
 * y presencia.
 */
public class SimpleMemoryManager implements MemoryManager {

    /** Memoria física que administra el gestor. */
    private final PhysicalMemory pm;

    /**
     * Constructor de SimpleMemoryManager.
     * Valida parámetros y enlaza la memoria física a administrar.
     * 
     * @param pm memoria física usada por el simulador
     */
    public SimpleMemoryManager(PhysicalMemory pm) {
        if (pm == null)
            throw new IllegalArgumentException("Memoria física no puede ser nula.");
        if (pm.pageSize <= 0)
            throw new IllegalArgumentException("Tamaño de página inválido.");
        this.pm = pm;
    }

    /**
     * Admite un proceso en memoria.
     * Asigna un marco por cada página lógica usando first-fit.
     * 
     * Reglas:
     * - Si el proceso ya estaba admitido, lanza error.
     * - Si hay suficientes marcos, se asignan todos.
     * - Si no, no se asigna nada y retorna false.
     *
     * @param pcb proceso a admitir
     * @return true si el proceso fue admitido, false si no había marcos suficientes
     */
    @Override
    public boolean admitProcess(PCB pcb) {
        if (pcb == null)
            throw new IllegalArgumentException("PCB no puede ser nulo.");
        for (PageTableEntry e : pcb.pageTable) {
            if (e.present)
                throw new IllegalStateException("El proceso ya está admitido en memoria.");
        }

        int need = pcb.pageCount;
        if (need == 0) {
            // Proceso sin páginas: se considera admitido directamente
            return true;
        }

        // Contar marcos libres disponibles
        int freeCount = 0;
        for (Frame f : pm.frames)
            if (f.free)
                freeCount++;
        if (freeCount < need)
            return false;

        // Seleccionar los primeros 'need' marcos libres
        int[] chosen = new int[need];
        int idx = 0;
        for (Frame f : pm.frames) {
            if (f.free) {
                chosen[idx++] = f.frameNumber;
                if (idx == need)
                    break;
            }
        }
        // Asignar cada página lógica a un marco físico
        for (int p = 0; p < pcb.pageCount; p++) {
            int frameNo = chosen[p];
            Frame f = pm.frames[frameNo];

            // Actualizar marco físico
            f.free = false;
            f.pid = pcb.pid;
            f.pageNumber = p;

            // Actualizar entrada de tabla de páginas
            PageTableEntry e = pcb.pageTable[p];
            e.frameNumber = frameNo;
            e.present = true;
            e.referenced = false;
            e.dirty = false;
        }
        return true;
    }

    /**
     * Libera todos los marcos ocupados por un proceso.
     * Además, limpia la tabla de páginas asociada al proceso.
     * 
     * @param pcb proceso cuyos marcos deben liberarse
     */
    @Override
    public void releaseProcess(PCB pcb) {
        if (pcb == null)
            throw new IllegalArgumentException("PCB no puede ser nulo.");

        // Liberar marcos físicos asociados al proceso
        for (Frame f : pm.frames) {
            if (!f.free && f.pid != null && f.pid == pcb.pid) {
                f.free = true;
                f.pid = null;
                f.pageNumber = null;
            }
        }

        // Limpiar la tabla de páginas del proceso
        for (PageTableEntry e : pcb.pageTable) {
            e.frameNumber = null;
            e.present = false;
            e.referenced = false;
            e.dirty = false;
        }
    }

    /**
     * Traduce una dirección lógica a dirección física.
     * Pasos:
     * - Calcular página y desplazamiento.
     * - Validar que la página esté dentro de rango.
     * - Verificar que la página esté presente en memoria.
     * - Retornar dirección física absoluta.
     * 
     * @param pcb         proceso al que pertenece la dirección
     * @param logicalAddr dirección lógica en bytes (>=0)
     * @return dirección física correspondiente
     * @throws IllegalArgumentException si la página está fuera de rango o no está
     *                                  presente
     */
    @Override
    public int mapLogicalToPhysical(PCB pcb, int logicalAddr) throws IllegalArgumentException {
        if (pcb == null)
            throw new IllegalArgumentException("PCB no puede ser nulo.");
        if (logicalAddr < 0)
            throw new IllegalArgumentException("La dirección lógica debe ser ≥ 0.");

        // Descomponer dirección lógica
        int page = logicalAddr / pm.pageSize;
        int offset = logicalAddr % pm.pageSize;

        // Validar rango
        if (page < 0 || page >= pcb.pageCount) {
            throw new IllegalArgumentException("Página fuera de rango para este proceso.");
        }

        // Verificar presencia
        PageTableEntry e = pcb.pageTable[page];
        if (!e.present || e.frameNumber == null) {
            throw new IllegalArgumentException("La página no está presente en memoria.");
        }

        // Marcar como referenciada
        e.referenced = true;

        // Calcular dirección física
        return e.frameNumber * pm.pageSize + offset;
    }
}
