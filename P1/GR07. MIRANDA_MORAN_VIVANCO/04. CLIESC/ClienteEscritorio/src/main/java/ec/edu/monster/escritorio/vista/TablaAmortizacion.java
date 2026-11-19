package ec.edu.monster.escritorio.vista;

import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import java.util.List;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Tabla de solo lectura para mostrar una tabla de amortizacion. */
public class TablaAmortizacion extends JTable {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] {"Cuota", "Fecha", "Cuota $", "Interés", "Capital", "Saldo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public TablaAmortizacion() {
        setModel(modelo);
        setAutoCreateRowSorter(false);
        Estilo.tabla(this, 8);
        getColumnModel().getColumn(0).setCellRenderer(Estilo.celda(SwingConstants.CENTER, 8));
        getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.LEFT, 8));
        for (int i = 2; i < 6; i++) {
            getColumnModel().getColumn(i).setCellRenderer(Estilo.celda(SwingConstants.RIGHT, 8));
        }
        getColumnModel().getColumn(0).setPreferredWidth(56);
        getColumnModel().getColumn(0).setMaxWidth(80);
        getColumnModel().getColumn(1).setMinWidth(104);
    }

    public void mostrar(List<CuotaAmortizacion> filas) {
        modelo.setRowCount(0);
        for (CuotaAmortizacion c : filas) {
            modelo.addRow(new Object[] {c.getNumCuota(), c.getFechaPago(), dos(c.getValorCuota()), dos(c.getInteresPagado()),
                    dos(c.getCapitalPagado()), dos(c.getSaldo())});
        }
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }
}
