package co.edu.uptc.view;

import java.util.List;
import co.edu.uptc.view.vm.FrameVM;
import co.edu.uptc.view.vm.PageEntryVM;
import co.edu.uptc.view.vm.ProcessVM;

/**
 * Interfaz MemoryView
 * ----------------------------------------------------
 * Contrato de la Vista dentro del patrón MVP.
 *
 * Responsabilidades:
 * - Mostrar información enviada por el Presenter en forma
 * de DTOs (ViewModels: FrameVM, ProcessVM, PageEntryVM).
 * - Emitir eventos de interacción del usuario hacia el Presenter.
 *
 * Importante:
 * - La vista no depende directamente del Modelo.
 * - Toda la comunicación con el Modelo se hace a través del Presenter.
 */
public interface MemoryView {

    /**
     * Renderiza el estado de los marcos físicos en la interfaz gráfica.
     * 
     * @param frames lista de marcos en formato FrameVM
     */
    void renderFrames(List<FrameVM> frames);

    /**
     * Renderiza la lista de procesos en la tabla de procesos.
     * 
     * @param processes lista de procesos en formato ProcessVM
     */
    void renderProcessList(List<ProcessVM> processes);

    /**
     * Renderiza la tabla de páginas de un proceso seleccionado.
     * 
     * @param pid         identificador del proceso
     * @param pageEntries lista de entradas de tabla de páginas en PageEntryVM
     */
    void renderPageTable(int pid, List<PageEntryVM> pageEntries);

    /**
     * Muestra un mensaje informativo en la interfaz.
     * (Por ejemplo, en un cuadro de diálogo de tipo información).
     * 
     * @param message texto del mensaje
     */
    void showInfo(String message);

    /**
     * Muestra un mensaje de error en la interfaz.
     * (Por ejemplo, en un cuadro de diálogo de error).
     * 
     * @param message texto del error
     */
    void showError(String message);

    /**
     * Establece dinámicamente el título de la columna de páginas en la tabla
     * de procesos. Se usa para mostrar, por ejemplo: "Páginas (1000B)".
     * 
     * @param title texto del título que se mostrará en la columna
     */
    void setPagesColumnTitle(String title);

    /**
     * Registra el Listener que permite a la vista notificar
     * los eventos del usuario al Presenter.
     * 
     * @param l implementación de la interfaz Listener
     */
    void setListener(Listener l);

    /**
     * Interfaz Listener
     * ------------------------------------------------
     * Define todos los eventos que la Vista puede notificar
     * al Presenter:
     * - Operaciones manuales sobre procesos.
     * - Controles de la simulación automática.
     */
    interface Listener {

        // ==== Operaciones manuales ====

        /** Crear un nuevo proceso con PID y tamaño en bytes. */
        void onCreateProcess(int pid, int sizeBytes);

        /** Admitir un proceso en memoria (asignación de marcos). */
        void onAdmitProcess(int pid);

        /** Suspender un proceso (libera sus marcos). */
        void onSuspendProcess(int pid);

        /** Reanudar un proceso suspendido (intenta admitirlo otra vez). */
        void onResumeProcess(int pid);

        /** Terminar un proceso y liberar todos sus recursos. */
        void onTerminateProcess(int pid);

        /** Acceder a una dirección lógica de un proceso (traducción). */
        void onAccessAddress(int pid, int logicalAddr);

        /** Seleccionar un proceso en la interfaz (ej: tabla). */
        void onSelectProcess(int pid);

        // ==== Controles de simulación automática ====

        /** Inicia la simulación automática de planificación. */
        void onStartSimulation();

        /** Pausa temporalmente la simulación. */
        void onPauseSimulation();

        /** Reanuda la simulación pausada. */
        void onResumeSimulation();

        /** Detiene por completo la simulación. */
        void onStopSimulation();
    }
}
