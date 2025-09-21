package co.edu.uptc.presenter;

import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import co.edu.uptc.model.*;
import co.edu.uptc.view.MemoryView;
import co.edu.uptc.view.MainWindow;
import co.edu.uptc.view.vm.FrameVM;
import co.edu.uptc.view.vm.PageEntryVM;
import co.edu.uptc.view.vm.ProcessVM;

/**
 * Clase MemoryPresenter
 * ------------------------------------------------------------
 * Es el único Presenter dentro del patrón MVP.
 * 
 * Responsabilidades principales:
 * - Mantener el estado del modelo (procesos, memoria física, gestor).
 * - Convertir el modelo en DTOs (ViewModels) que la vista pueda mostrar.
 * - Manejar todos los eventos de la interfaz gráfica:
 * tanto las operaciones manuales como la simulación automática.
 * 
 * Notas importantes:
 * - La vista (UI) nunca accede directamente al modelo, siempre a través
 * de este Presenter.
 * - El Presenter coordina la comunicación entre Modelo y Vista.
 * - El método `main` de la aplicación está aquí, no en la vista.
 */
public class MemoryPresenter implements MemoryView.Listener {

    // --- Configuración general ---

    /** Tamaño de página en bytes (usado para segmentar direcciones). */
    private final int PAGE_SIZE = 1000;

    /** Número de marcos disponibles en la memoria física simulada. */
    private final int NUM_FRAMES = 25;

    /** Duración (ms) de cada tick de simulación visual. */
    private final int QUANTUM_MS = 700;

    /** Costo de ejecución por página: ticks requeridos. */
    private final int TICKS_PER_PAGE = 2;

    // --- Modelo ---

    /** Memoria física simulada. */
    private final PhysicalMemory pm = new PhysicalMemory(PAGE_SIZE, NUM_FRAMES);

    /** Gestor de memoria encargado de asignar/liberar marcos. */
    private final MemoryManager mm = new SimpleMemoryManager(pm);

    /** Procesos actualmente vivos, indexados por PID. */
    private final Map<Integer, PCB> processes = new LinkedHashMap<>();

    // --- Vista ---

    /** Referencia a la vista principal. */
    private final MemoryView view;

    /**
     * Proceso actualmente seleccionado en la UI (para mostrar su tabla de páginas).
     */
    private PCB selected = null;

    // --- Estado de la simulación ---

    /** Cola de listos (READY), contiene PIDs para round-robin. */
    private final Deque<Integer> readyQueue = new ArrayDeque<>();

    /** Tiempo de ejecución restante por proceso (en ticks). */
    private final Map<Integer, Integer> remainingTicks = new HashMap<>();

    /** Temporizador que dispara cada tick de la simulación. */
    private Timer simTimer;

    /** Bandera: indica si la simulación está en ejecución. */
    private boolean simRunning = false;

    /** Bandera: indica si la simulación está en pausa. */
    private boolean simPaused = false;

    /** PID del proceso que está actualmente en RUNNING. */
    private Integer runningPid = null;

    /**
     * Constructor del Presenter.
     * - Vincula el listener con la vista.
     * - Actualiza el título dinámico de la columna de páginas.
     * - Carga datos de demostración.
     * - Refresca la interfaz inicial.
     */
    public MemoryPresenter(MemoryView view) {
        this.view = view;
        this.view.setListener(this);

        //título dinámico de la columna de páginas
        this.view.setPagesColumnTitle("Páginas (" + PAGE_SIZE + "B)");

        seedDemoData();
        refreshAll();
    }

    // ===== Datos iniciales =====

    /** Crea procesos de ejemplo para pruebas (solo si aún no existen). */
    private void seedDemoData() {
        if (!processes.isEmpty())
            return;

        int[] pages = { 4, 6, 3, 8, 5, 7, 2, 10 };
        for (int i = 0; i < pages.length; i++) {
            int pid = i + 1;
            int sizeBytes = pages[i] * PAGE_SIZE;
            PCB pcb = new PCB(pid, sizeBytes, PAGE_SIZE);
            processes.put(pid, pcb);
            remainingTicks.put(pid, Math.max(1, pages[i] * TICKS_PER_PAGE));
        }
        selected = processes.get(1);
    }

    // ===== Adaptadores Modelo → DTO =====

    /** Convierte marcos del modelo en FrameVM para la vista. */
    private List<FrameVM> toFrameVMs() {
        List<FrameVM> out = new ArrayList<>();
        for (Frame f : pm.frames) {
            String pidText = (f.pid == null) ? "-" : String.valueOf(f.pid);
            String pageText = (f.pageNumber == null) ? "-" : String.valueOf(f.pageNumber);
            out.add(new FrameVM(f.frameNumber, f.free, pidText, pageText));
        }
        return out;
    }

    /** Convierte procesos en ProcessVM para la tabla de procesos. */
    private List<ProcessVM> toProcessVMs() {
        List<ProcessVM> out = new ArrayList<>();
        for (PCB p : processes.values()) {
            out.add(new ProcessVM(p.pid, p.state.name(), p.logicalSizeBytes, p.pageCount));
        }
        return out;
    }

    /** Convierte entradas de tabla de páginas en PageEntryVM. */
    private List<PageEntryVM> toPageEntryVMs(PCB pcb) {
        List<PageEntryVM> out = new ArrayList<>();
        for (PageTableEntry e : pcb.pageTable) {
            String frameText = (e.frameNumber == null) ? "-" : String.valueOf(e.frameNumber);
            out.add(new PageEntryVM(e.pageNumber, e.present, frameText, e.referenced, e.dirty));
        }
        return out;
    }

    // ===== Render =====

    /**
     * Refresca todos los paneles de la UI (marcos, procesos, tabla seleccionada).
     */
    private void refreshAll() {
        view.renderFrames(toFrameVMs());
        view.renderProcessList(toProcessVMs());
        if (selected != null)
            view.renderPageTable(selected.pid, toPageEntryVMs(selected));
    }

    /** Refresca solo la tabla del proceso actualmente seleccionado. */
    private void refreshSelected() {
        if (selected != null)
            view.renderPageTable(selected.pid, toPageEntryVMs(selected));
    }

    /** Obtiene un PCB por PID o lanza excepción si no existe. */
    private PCB require(int pid) {
        PCB pcb = processes.get(pid);
        if (pcb == null)
            throw new IllegalArgumentException("PID desconocido: " + pid);
        return pcb;
    }

    // ===== Admisión =====

    /** Admisión greedy: admite todos los NEW/SUSP_READY que quepan en memoria. */
    private boolean greedyAdmit() {
        boolean admitted = false;
        for (PCB pcb : processes.values()) {
            if (pcb.state == ProcessState.NEW || pcb.state == ProcessState.SUSP_READY) {
                if (mm.admitProcess(pcb)) {
                    pcb.state = ProcessState.READY;
                    readyQueue.addLast(pcb.pid);
                    admitted = true;
                }
            }
        }
        return admitted;
    }

    // ===== Simulación =====

    @Override
    public void onStartSimulation() {
        if (simRunning && !simPaused) {
            view.showInfo("La simulación ya está en ejecución.");
            return;
        }
        if (simRunning && simPaused) {
            onResumeSimulation();
            return;
        }
        readyQueue.clear();
        greedyAdmit();
        if (readyQueue.isEmpty()) {
            view.showInfo("No hay procesos listos. Crea o admite alguno primero.");
            return;
        }
        simTimer = new Timer(QUANTUM_MS, e -> step());
        simTimer.start();
        simRunning = true;
        simPaused = false;
        view.showInfo("Simulación iniciada.");
    }

    @Override
    public void onPauseSimulation() {
        if (!simRunning || simPaused) {
            view.showInfo("La simulación no se puede pausar ahora.");
            return;
        }
        simTimer.stop();
        simPaused = true;
        if (runningPid != null) {
            PCB pcb = processes.get(runningPid);
            if (pcb != null && pcb.state == ProcessState.RUNNING)
                pcb.state = ProcessState.READY;
            readyQueue.addFirst(runningPid);
            runningPid = null;
        }
        refreshAll();
        view.showInfo("Simulación en pausa.");
    }

    @Override
    public void onResumeSimulation() {
        if (!simRunning || !simPaused) {
            view.showInfo("La simulación no está en pausa.");
            return;
        }
        if (readyQueue.isEmpty())
            greedyAdmit();
        simTimer.start();
        simPaused = false;
        view.showInfo("Simulación reanudada.");
    }

    @Override
    public void onStopSimulation() {
        if (!simRunning) {
            view.showInfo("La simulación ya está detenida.");
            return;
        }
        simTimer.stop();
        simRunning = false;
        simPaused = false;
        if (runningPid != null) {
            PCB pcb = processes.get(runningPid);
            if (pcb != null && pcb.state == ProcessState.RUNNING)
                pcb.state = ProcessState.READY;
            readyQueue.addFirst(runningPid);
            runningPid = null;
        }
        refreshAll();
        view.showInfo("Simulación detenida.");
    }

    /** Ejecuta un tick: RUNNING consume tiempo, termina o vuelve a READY. */
    private void step() {
        if (readyQueue.isEmpty()) {
            boolean admitted = greedyAdmit();
            if (!admitted) {
                simTimer.stop();
                simRunning = false;
                view.showInfo("Simulación finalizada: no quedan procesos por ejecutar.");
                refreshAll();
                return;
            }
        }
        Integer pid = readyQueue.pollFirst();
        runningPid = pid;
        PCB pcb = processes.get(pid);
        if (pcb == null)
            return;

        pcb.state = ProcessState.RUNNING;
        refreshAll();

        int left = remainingTicks.getOrDefault(pid, Math.max(1, pcb.pageCount * TICKS_PER_PAGE));
        left -= 1;
        remainingTicks.put(pid, Math.max(0, left));

        if (left <= 0) {
            mm.releaseProcess(pcb);
            pcb.state = ProcessState.TERMINATED;
            runningPid = null;
            greedyAdmit();
            refreshAll();
            return;
        }

        pcb.state = ProcessState.READY;
        runningPid = null;
        readyQueue.addLast(pid);
        refreshAll();
    }

    // ===== Eventos manuales =====

    @Override
    public void onCreateProcess(int pid, int sizeBytes) {
        try {
            if (pid < 0 || sizeBytes <= 0) {
                view.showError("PID y Tamaño deben ser positivos.");
                return;
            }
            if (processes.containsKey(pid)) {
                view.showError("El PID ya existe.");
                return;
            }
            PCB pcb = new PCB(pid, sizeBytes, PAGE_SIZE);
            processes.put(pid, pcb);
            remainingTicks.put(pid, Math.max(1, pcb.pageCount * TICKS_PER_PAGE));
            if (selected == null)
                selected = pcb;
            view.showInfo("Proceso " + pid + " creado: " + sizeBytes + " B (" + pcb.pageCount + " páginas).");
            refreshAll();
        } catch (Exception ex) {
            view.showError("Fallo al crear: " + ex.getMessage());
        }
    }

    @Override
    public void onAdmitProcess(int pid) {
        try {
            PCB pcb = require(pid);
            switch (pcb.state) {
                case READY, RUNNING -> {
                    view.showError("El proceso ya está admitido en memoria.");
                    return;
                }
                case TERMINATED -> {
                    view.showError("El proceso está TERMINADO.");
                    return;
                }
                default -> {
                }
            }
            boolean ok = mm.admitProcess(pcb);
            if (ok) {
                pcb.state = ProcessState.READY;
                view.showInfo("Proceso " + pid + " admitido en memoria.");
            } else {
                pcb.state = ProcessState.SUSP_READY;
                view.showInfo("No hay marcos suficientes. Proceso " + pid + " a SUSP_READY.");
            }
            refreshAll();
        } catch (IllegalStateException ise) {
            view.showError(ise.getMessage());
        } catch (Exception ex) {
            view.showError("Fallo al admitir: " + ex.getMessage());
        }
    }

    @Override
    public void onSuspendProcess(int pid) {
        try {
            PCB pcb = require(pid);
            mm.releaseProcess(pcb);
            pcb.state = ProcessState.SUSP_READY;
            readyQueue.remove(pid);
            view.showInfo("Proceso " + pid + " suspendido. Marcos liberados.");
            refreshAll();
        } catch (Exception ex) {
            view.showError("Fallo al suspender: " + ex.getMessage());
        }
    }

    @Override
    public void onResumeProcess(int pid) {
        onAdmitProcess(pid);
    }

    @Override
    public void onTerminateProcess(int pid) {
        try {
            PCB pcb = require(pid);
            mm.releaseProcess(pcb);
            pcb.state = ProcessState.TERMINATED;
            readyQueue.remove(pid);
            remainingTicks.put(pid, 0);
            view.showInfo("Proceso " + pid + " terminado. Marcos liberados.");
            refreshAll();
        } catch (Exception ex) {
            view.showError("Fallo al terminar: " + ex.getMessage());
        }
    }

    @Override
    public void onAccessAddress(int pid, int logicalAddr) {
        try {
            if (logicalAddr < 0) {
                view.showError("La dirección lógica debe ser ≥ 0.");
                return;
            }
            PCB pcb = require(pid);
            int physical = mm.mapLogicalToPhysical(pcb, logicalAddr);
            int page = logicalAddr / PAGE_SIZE;
            int offset = logicalAddr % PAGE_SIZE;
            String msg = "Proceso No: " + pid + "\n"
                    + "Hoja No: " + page + "\n"
                    + "Dirección lógica No: " + logicalAddr + "\n"
                    + "Desplazamiento: " + offset + "\n"
                    + "Dirección física: " + physical;
            view.showInfo(msg);
            refreshSelected();
        } catch (IllegalArgumentException iae) {
            view.showError("Error de mapeo: " + iae.getMessage());
        } catch (Exception ex) {
            view.showError("Fallo al acceder: " + ex.getMessage());
        }
    }

    @Override
    public void onSelectProcess(int pid) {
        try {
            selected = require(pid);
            refreshSelected();
        } catch (Exception ex) {
            view.showError("Fallo al seleccionar: " + ex.getMessage());
        }
    }

    // ===== Punto de entrada =====

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow w = new MainWindow();
            new MemoryPresenter(w);
            if (w instanceof javax.swing.JFrame frame)
                frame.setVisible(true);
        });
    }
}
