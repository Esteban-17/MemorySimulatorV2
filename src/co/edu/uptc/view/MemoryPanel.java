package co.edu.uptc.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import co.edu.uptc.view.vm.FrameVM;

/**
 * Clase MemoryPanel
 * -------------------------------------------------------
 * Panel que muestra la memoria física como tabla de marcos.
 * La columna booleana ahora indica "Ocupado" (true si el marco
 * está asignado a algún proceso; false si está libre).
 */
public class MemoryPanel extends JPanel {

    /** Tabla que muestra los marcos de memoria física. */
    private final JTable table;

    /** Modelo de la tabla, utilizado para manejar filas y columnas. */
    private final DefaultTableModel model;

    /**
     * Constructor de MemoryPanel.
     * Configura la tabla y su modelo para mostrar marcos de memoria.
     */
    public MemoryPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Memoria Física (Marcos)"));

        // Encabezados: "Marco", "Ocupado", "PID", "Página"
        model = new DefaultTableModel(new Object[] { "Marco", "Ocupado", "PID", "Página" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Integer.class; // número de marco
                if (columnIndex == 1)
                    return Boolean.class; // ocupado (checkbox)
                return Object.class; // PID / Página
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true); // permite ordenar columnas

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Actualiza la tabla con el estado actual de los marcos.
     * NOTA: "Ocupado" = !free.
     *
     * @param frames lista de marcos en formato FrameVM
     */
    public void updateFrames(List<FrameVM> frames) {
        model.setRowCount(0);
        for (FrameVM f : frames) {
            boolean ocupado = !f.free; // invertimos la lógica para la nueva columna
            model.addRow(new Object[] { f.frameNumber, ocupado, f.pidText, f.pageText });
        }
    }
}
