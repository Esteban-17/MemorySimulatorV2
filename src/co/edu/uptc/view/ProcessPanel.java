package co.edu.uptc.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import co.edu.uptc.view.vm.ProcessVM;

/**
 * Clase ProcessPanel
 * ------------------------------------------------------
 * Panel responsable de:
 * - Mostrar la lista de procesos (PID, estado, tamaño y páginas) en una tabla.
 * - Ofrecer controles manuales (crear/admitir/suspender/reanudar/terminar).
 * - Ofrecer controles de simulación (iniciar/pausar/reanudar/detener).
 *
 * Notas:
 * - No tiene dependencia con el modelo. Solo trabaja con DTOs (ProcessVM).
 * - La comunicación hacia la lógica se hace vía MemoryView.Listener.
 */
public class ProcessPanel extends JPanel {

    /** Listener (contrato de la vista) para notificar acciones del usuario al Presenter. */
    private MemoryView.Listener listener;

    /** Tabla donde se listan los procesos. */
    private final JTable table;

    /** Modelo subyacente de la tabla (manejo de filas/columnas). */
    private final DefaultTableModel model;

    /** Campo para ingresar el PID al crear un proceso. */
    private final JTextField pidField = new JTextField(5);

    /** Campo para ingresar el tamaño en bytes al crear un proceso. */
    private final JTextField sizeField = new JTextField(7);

    /**
     * Constructor: configura estructura, tabla, controles y acciones.
     */
    public ProcessPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Procesos"));

        // === Tabla central de procesos ===
        // Columnas por defecto; el título de "Páginas" puede actualizarse dinámicamente
        // mediante setPagesColumnTitle(...) cuando el Presenter lo indique.
        model = new DefaultTableModel(new Object[] { "PID", "Estado", "Tamaño (B)", "Páginas" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // todas las celdas son de solo lectura
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3)
                    return Integer.class; // PID, Tamaño, Páginas como enteros para ordenamiento correcto
                return Object.class;
            }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Controles superiores (manuales) ===
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        topControls.add(new JLabel("PID:"));
        topControls.add(pidField);
        topControls.add(new JLabel("Tamaño (B):"));
        topControls.add(sizeField);

        JButton createBtn = new JButton("Crear");
        JButton admitBtn = new JButton("Admitir");
        JButton suspendBtn = new JButton("Suspender");
        JButton resumeBtn = new JButton("Reanudar");
        JButton terminateBtn = new JButton("Terminar");

        topControls.add(createBtn);
        topControls.add(admitBtn);
        topControls.add(suspendBtn);
        topControls.add(resumeBtn);
        topControls.add(terminateBtn);
        add(topControls, BorderLayout.NORTH);

        // === Controles inferiores (simulación) ===
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton startSimBtn = new JButton("Iniciar simulación");
        JButton pauseSimBtn = new JButton("Pausar");
        JButton contSimBtn = new JButton("Reanudar");
        JButton stopSimBtn = new JButton("Detener");

        bottomControls.add(startSimBtn);
        bottomControls.add(pauseSimBtn);
        bottomControls.add(contSimBtn);
        bottomControls.add(stopSimBtn);
        add(bottomControls, BorderLayout.SOUTH);

        // === Acciones de botones (manuales) ===
        createBtn.addActionListener(e -> {
            if (listener == null) return;
            try {
                int pid = Integer.parseInt(pidField.getText().trim());
                int size = Integer.parseInt(sizeField.getText().trim());
                listener.onCreateProcess(pid, size);
            } catch (NumberFormatException ignore) {
                // Si la entrada no es numérica, se ignora silenciosamente.
            }
        });
        admitBtn.addActionListener(e -> withSelectedPid(pid -> listener.onAdmitProcess(pid)));
        suspendBtn.addActionListener(e -> withSelectedPid(pid -> listener.onSuspendProcess(pid)));
        resumeBtn.addActionListener(e -> withSelectedPid(pid -> listener.onResumeProcess(pid)));
        terminateBtn.addActionListener(e -> withSelectedPid(pid -> listener.onTerminateProcess(pid)));

        // === Acciones de botones (simulación) ===
        startSimBtn.addActionListener(e -> { if (listener != null) listener.onStartSimulation(); });
        pauseSimBtn.addActionListener(e -> { if (listener != null) listener.onPauseSimulation(); });
        contSimBtn.addActionListener(e -> { if (listener != null) listener.onResumeSimulation(); });
        stopSimBtn.addActionListener(e -> { if (listener != null) listener.onStopSimulation(); });

        // === Evento de selección en la tabla ===
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    int pid = (int) model.getValueAt(modelRow, 0);
                    listener.onSelectProcess(pid);
                }
            }
        });
    }

    /**
     * Interfaz funcional interna para ejecutar una acción con el PID seleccionado.
     */
    private interface PidAction {
        void run(int pid);
    }

    /**
     * Obtiene el PID seleccionado en la tabla y ejecuta la acción indicada.
     * Si no hay selección o no hay listener, no hace nada.
     */
    private void withSelectedPid(PidAction action) {
        int row = table.getSelectedRow();
        if (row >= 0 && listener != null) {
            int modelRow = table.convertRowIndexToModel(row);
            int pid = (int) model.getValueAt(modelRow, 0);
            action.run(pid);
        }
    }

    /**
     * Actualiza la lista de procesos en la tabla (borra y vuelve a cargar).
     * Se invoca desde la vista principal cuando el Presenter entrega ProcessVMs.
     */
    public void updateProcessList(List<ProcessVM> processes) {
        model.setRowCount(0);
        for (ProcessVM p : processes) {
            model.addRow(new Object[] { p.pid, p.state, p.sizeBytes, p.pages });
        }
    }

    /**
     * NUEVO: Permite cambiar dinámicamente el título de la columna "Páginas"
     * para reflejar el tamaño de página actual, por ejemplo: "Páginas (1000B)".
     * El Presenter la invoca al construirse (y podría hacerlo cada vez que cambie PAGE_SIZE).
     */
    public void setPagesColumnTitle(String title) {
        // La columna de páginas es la de índice 3 en el modelo por defecto.
        table.getColumnModel().getColumn(3).setHeaderValue(title);
        // Forzamos repintado del header para que el cambio sea visible inmediatamente.
        table.getTableHeader().repaint();
    }

    /**
     * Registra el listener (canal de salida de eventos de la vista hacia el Presenter).
     */
    public void setListener(MemoryView.Listener l) {
        this.listener = l;
    }
}
