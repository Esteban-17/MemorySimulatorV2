package co.edu.uptc.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import co.edu.uptc.view.vm.PageEntryVM;

/**
 * Clase MappingPanel
 * ----------------------------------------------------------------
 * Panel de la interfaz gráfica que muestra la tabla de páginas de un
 * proceso seleccionado y permite realizar el mapeo de direcciones lógicas.
 * 
 * Funcionalidad:
 * - Visualizar cada página lógica, si está presente, el marco asignado,
 * y los bits de referencia (Ref) y modificado (Modif.).
 * - Permitir ingresar una dirección lógica y solicitar al Presenter
 * su traducción a dirección física.
 */
public class MappingPanel extends JPanel {

    /** Listener que conecta la vista con el Presenter (MVP). */
    private MemoryView.Listener listener;

    /** Tabla que muestra la tabla de páginas del proceso activo. */
    private final JTable table;

    /** Modelo de la tabla, usado para manejar las filas de la tabla de páginas. */
    private final DefaultTableModel model;

    /** Campo de texto para ingresar la dirección lógica a mapear. */
    private final JTextField addrField = new JTextField(12);

    /** Botón que ejecuta la acción de mapeo de la dirección lógica. */
    private final JButton mapBtn = new JButton("Mapear");

    /**
     * PID del proceso actualmente seleccionado (se establece en updatePageTable).
     */
    private Integer currentPid = null;

    /**
     * Constructor de MappingPanel.
     * Configura la tabla de páginas y el panel inferior para ingresar direcciones.
     */
    public MappingPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tabla de Páginas y Mapeo de Dirección"));

        // Configuración del modelo de tabla
        // Nuevo modelo sin "Modif."
        model = new DefaultTableModel(
                new Object[] { "Página", "Presente", "Marco", "Ref" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Integer.class;
                if (columnIndex == 1 || columnIndex == 3)
                    return Boolean.class;
                return Object.class;
            }
        };

        // Tabla de páginas
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel inferior: entrada de dirección lógica
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Dirección lógica:"));
        bottom.add(addrField);
        bottom.add(mapBtn);
        add(bottom, BorderLayout.SOUTH);

        // Acción del botón "Mapear"
        mapBtn.addActionListener(e -> {
            if (listener == null || currentPid == null)
                return;
            try {
                int addr = Integer.parseInt(addrField.getText().trim());
                listener.onAccessAddress(currentPid, addr);
            } catch (NumberFormatException ignore) {
                // Si la dirección no es un número válido, simplemente se ignora
            }
        });
    }

    /**
     * Actualiza la tabla de páginas del proceso activo.
     * 
     * @param pid     identificador del proceso
     * @param entries lista de entradas de la tabla de páginas en formato
     *                PageEntryVM
     */
    public void updatePageTable(int pid, List<PageEntryVM> entries) {
        currentPid = pid;
        model.setRowCount(0); // limpiar tabla
        for (PageEntryVM e : entries) {
            model.addRow(new Object[] { e.page, e.present, e.frameText, e.referenced });
        }
    }

    /**
     * Registra el listener que comunicará los eventos al Presenter.
     * 
     * @param l implementación de MemoryView.Listener
     */
    public void setListener(MemoryView.Listener l) {
        this.listener = l;
    }
}
