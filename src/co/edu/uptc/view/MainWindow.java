package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import co.edu.uptc.view.vm.FrameVM;
import co.edu.uptc.view.vm.PageEntryVM;
import co.edu.uptc.view.vm.ProcessVM;

/**
 * Clase MainWindow
 * -------------------------------------------------------------------
 * Ventana principal de la aplicación.
 * Implementa la interfaz MemoryView para integrarse con el Presenter.
 *
 * Funcionalidad:
 * - Organiza los tres paneles principales de la interfaz:
 * * ProcessPanel (izquierda): gestión manual y simulación de procesos.
 * * MemoryPanel (arriba derecha): estado de marcos físicos.
 * * MappingPanel (abajo derecha): tabla de páginas y mapeo de direcciones.
 * - Recibe DTOs desde el Presenter para actualizar la UI sin exponer el modelo.
 * - Envía eventos del usuario al Presenter a través del Listener.
 * - (NUEVO) Permite actualizar dinámicamente el título de la columna "Páginas"
 * con el tamaño de página actual (ej.: "Páginas (1000B)").
 */
public class MainWindow extends JFrame implements MemoryView {

    /** Listener que conecta la vista con el Presenter (canal de eventos de UI). */
    @SuppressWarnings("unused")
    private Listener listener;

    /** Panel que muestra los marcos de memoria física. */
    private final MemoryPanel memoryPanel = new MemoryPanel();

    /** Panel que lista procesos y controles de ejecución/simulación. */
    private final ProcessPanel processPanel = new ProcessPanel();

    /** Panel que muestra tabla de páginas y permite mapear direcciones. */
    private final MappingPanel mappingPanel = new MappingPanel();

    /**
     * Constructor de MainWindow.
     * Configura la ventana principal y organiza los paneles en un diseño dividido.
     */
    public MainWindow() {
        super("Simulador de Paginación de Memoria (MVP)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 650);
        setLocationRelativeTo(null); // centrar ventana

        // División vertical derecha: memoria (arriba) y mapeo (abajo)
        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, memoryPanel, mappingPanel);
        right.setResizeWeight(0.6); // ~60% superior
        right.setContinuousLayout(true);

        // División horizontal raíz: procesos (izquierda) y derecha (memoria+tabla)
        JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processPanel, right);
        root.setResizeWeight(0.32); // ~32% izquierda
        root.setContinuousLayout(true);

        // Agregar panel raíz al contenido de la ventana
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(root, BorderLayout.CENTER);
    }

    // --- Implementación de MemoryView (renderiza DTOs) ---

    /** Renderiza la lista de marcos físicos en el panel de memoria. */
    @Override
    public void renderFrames(List<FrameVM> frames) {
        memoryPanel.updateFrames(frames);
    }

    /** Renderiza la lista de procesos en el panel de procesos. */
    @Override
    public void renderProcessList(List<ProcessVM> processes) {
        processPanel.updateProcessList(processes);
    }

    /** Renderiza la tabla de páginas del proceso seleccionado. */
    @Override
    public void renderPageTable(int pid, List<PageEntryVM> pageEntries) {
        mappingPanel.updatePageTable(pid, pageEntries);
    }

    /** Muestra un mensaje informativo en un cuadro de diálogo. */
    @Override
    public void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra un mensaje de error en un cuadro de diálogo. */
    @Override
    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * (NUEVO) Actualiza dinámicamente el título de la columna "Páginas"
     * del ProcessPanel. El Presenter lo invoca para reflejar el page size.
     */
    @Override
    public void setPagesColumnTitle(String title) {
        processPanel.setPagesColumnTitle(title);
    }

    /** Registra el listener para comunicar eventos al Presenter. */
    @Override
    public void setListener(Listener l) {
        this.listener = l;
        processPanel.setListener(l);
        mappingPanel.setListener(l);
    }
}
