package ec.edu.monster.consola.vista;

import ec.edu.monster.cliente.Formato;
import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.Telefono;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/** Todo lo que se lee y se escribe en la consola. No conoce el servicio. */
public class VistaConsola {

    private final Scanner entrada;
    private final PrintStream salida;

    public VistaConsola(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void mensaje(String texto) {
        salida.println(texto);
    }

    /** Devuelve null si ya no hay mas entrada (fin de archivo). */
    public String pedir(String etiqueta) {
        salida.print(etiqueta + ": ");
        salida.flush();
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    /** Pide un entero; devuelve null si el usuario no escribe un numero valido. */
    public Integer pedirEntero(String etiqueta) {
        String s = pedir(etiqueta);
        try {
            return s == null ? null : Integer.valueOf(s);
        } catch (NumberFormatException e) {
            salida.println("Debe ingresar un numero entero.");
            return null;
        }
    }

    public Double pedirDecimal(String etiqueta) {
        String s = pedir(etiqueta);
        try {
            return s == null ? null : Double.valueOf(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            salida.println("Debe ingresar un numero (ejemplo 199.99).");
            return null;
        }
    }

    public String menu() {
        salida.println();
        salida.println("=== COMERCIALIZADORA - VENTA DE CELULARES ===");
        salida.println(" 1. Ver catalogo");
        salida.println(" 2. Agregar celular");
        salida.println(" 3. Modificar celular");
        salida.println(" 4. Eliminar celular");
        salida.println(" 5. Verificar si un cliente es sujeto de credito");
        salida.println(" 6. Vender en efectivo (42 % de descuento)");
        salida.println(" 7. Vender a credito");
        salida.println(" 8. Consultar facturas de un cliente");
        salida.println(" 9. Ver tabla de amortizacion de un credito");
        salida.println(" 0. Salir");
        return pedir("Opcion");
    }

    public void catalogo(List<Telefono> telefonos) {
        salida.println(String.format("%-5s %-12s %-24s %10s  %s", "Cod", "Marca", "Modelo", "Precio", "Disponible"));
        for (Telefono t : telefonos) {
            salida.println(String.format("%-5d %-12s %-24s %10s  %s", t.getCodTelefono(), t.getMarca(), t.getModelo(),
                    Formato.dinero(t.getPrecio()), t.isDisponible() ? "si" : "no"));
        }
    }

    public void evaluacion(EvaluacionCredito e, DetalleMontoMaximo d) {
        salida.println((e.isSujetoCredito() ? "SI" : "NO") + " es sujeto de credito: " + e.getMotivo());
        if (e.isSujetoCredito()) {
            salida.println("Cliente: " + d.getNombre());
            salida.println("Promedio de depositos (3 meses): " + Formato.dinero(d.getPromedioDepositos()));
            salida.println("Promedio de retiros (3 meses):   " + Formato.dinero(d.getPromedioRetiros()));
            salida.println("Monto maximo de credito:         " + Formato.dinero(d.getMontoMaximo()));
        }
    }

    public void factura(Factura f) {
        salida.println("---------------- FACTURA " + f.getNumFactura() + " ----------------");
        salida.println("Fecha:    " + f.getFecha());
        salida.println("Cliente:  " + f.getNombreCliente() + " (" + f.getCedula() + ")");
        salida.println("Celular:  " + f.getTelefono());
        salida.println("Pago:     " + f.getFormaPago()
                + (f.getNumCuotas() > 0 ? " a " + f.getNumCuotas() + " cuotas (credito " + f.getCodCredito() + ")" : ""));
        salida.println("Precio:   " + Formato.dinero(f.getPrecioLista()));
        salida.println("Descuento:" + String.format("%10s", Formato.dinero(f.getDescuento())));
        salida.println("TOTAL:    " + Formato.dinero(f.getTotal()));
    }

    public void facturas(List<Factura> lista) {
        if (lista.isEmpty()) {
            salida.println("El cliente no tiene facturas.");
            return;
        }
        for (Factura f : lista) {
            salida.println(String.format("#%-4d %s  %-24s %-8s %10s%s", f.getNumFactura(), f.getFecha(), f.getTelefono(),
                    f.getFormaPago(), Formato.dinero(f.getTotal()),
                    f.getCodCredito() > 0 ? "  (credito " + f.getCodCredito() + ")" : ""));
        }
    }

    public void amortizacion(List<CuotaAmortizacion> filas) {
        if (filas.isEmpty()) {
            salida.println("No existe ese credito.");
            return;
        }
        salida.print(Formato.tablaTexto(filas));
    }
}
