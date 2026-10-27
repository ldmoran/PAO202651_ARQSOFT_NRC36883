package ec.edu.monster.escritorio.vista;

import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import java.util.List;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Tabla de solo lectura para mostrar una tabla de amortizacion. */
public class TablaAmortizacion extends JTable {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] {"Cuota", "Fecha", "Valor cuota", "Interes", "Capital", "Saldo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public TablaAmortizacion() {
        setModel(modelo);
        setAutoCreateRowSorter(false);
    }

    public void mostrar(List<CuotaAmortizacion> filas) {
        modelo.setRowCount(0);
        for (CuotaAmortizacion c : filas) {
            modelo.addRow(new Object[] {c.getNumCuota(), c.getFechaPago(), dos(c.getValorCuota()), dos(c.getInteresPagado()),
                    dos(c.getCapitalPagado()), dos(c.getSaldo())});
        }
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}
