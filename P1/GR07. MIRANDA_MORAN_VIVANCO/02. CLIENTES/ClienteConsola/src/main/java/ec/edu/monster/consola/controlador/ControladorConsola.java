package ec.edu.monster.consola.controlador;

import ec.edu.monster.cliente.ComercializadoraSoap;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.ResultadoFactura;
import ec.edu.monster.cliente.modelos.Telefono;
import ec.edu.monster.consola.vista.VistaConsola;

/** Conecta la vista con el servicio: decide que hacer con cada opcion del menu. */
public class ControladorConsola {

    private final ComercializadoraSoap servicio;
    private final VistaConsola vista;

    public ControladorConsola(ComercializadoraSoap servicio, VistaConsola vista) {
        this.servicio = servicio;
        this.vista = vista;
    }

    public void ejecutar() {
        while (true) {
            String opcion = vista.menu();
            if (opcion == null || "0".equals(opcion)) {
                vista.mensaje("Hasta luego.");
                return;
            }
            try {
                atender(opcion);
            } catch (ComercializadoraSoap.ServicioNoDisponible e) {
                vista.mensaje("ERROR: " + e.getMessage());
            }
        }
    }

    private void atender(String opcion) {
        switch (opcion) {
            case "1" -> vista.catalogo(servicio.listarTelefonos());
            case "2" -> agregar();
            case "3" -> modificar();
            case "4" -> eliminar();
            case "5" -> verificarCredito();
            case "6" -> vender(false);
            case "7" -> vender(true);
            case "8" -> consultarFacturas();
            case "9" -> verAmortizacion();
            default -> vista.mensaje("Opcion no valida.");
        }
    }

    private void agregar() {
        String marca = vista.pedir("Marca");
        String modelo = vista.pedir("Modelo");
        Double precio = vista.pedirDecimal("Precio");
        if (marca == null || modelo == null || precio == null) {
            return;
        }
        int cod = servicio.crearTelefono(marca, modelo, precio);
        vista.mensaje(cod > 0 ? "Celular agregado con codigo " + cod + "." : "Datos invalidos: marca, modelo y precio mayor a cero.");
    }

    private void modificar() {
        Integer cod = vista.pedirEntero("Codigo del celular");
        if (cod == null) {
            return;
        }
        Telefono t = servicio.buscarTelefono(cod);
        if (t == null) {
            vista.mensaje("No existe ese celular.");
            return;
        }
        String marca = vista.pedir("Marca [" + t.getMarca() + "]");
        String modelo = vista.pedir("Modelo [" + t.getModelo() + "]");
        Double precio = vista.pedirDecimal("Precio [" + t.getPrecio() + "]");
        String disp = vista.pedir("Disponible (s/n) [" + (t.isDisponible() ? "s" : "n") + "]");
        if (marca == null || modelo == null || precio == null || disp == null) {
            return;
        }
        boolean ok = servicio.actualizarTelefono(cod, marca.isEmpty() ? t.getMarca() : marca,
                modelo.isEmpty() ? t.getModelo() : modelo, precio, disp.isEmpty() ? t.isDisponible() : disp.startsWith("s"));
        vista.mensaje(ok ? "Celular actualizado." : "No se pudo actualizar (revise los datos).");
    }

    private void eliminar() {
        Integer cod = vista.pedirEntero("Codigo del celular");
        if (cod == null) {
            return;
        }
        vista.mensaje(servicio.eliminarTelefono(cod)
                ? "Celular eliminado (si ya tenia facturas queda como no disponible)." : "No existe ese celular.");
    }

    private void verificarCredito() {
        String cedula = vista.pedir("Cedula");
        if (cedula == null) {
            return;
        }
        EvaluacionCredito e = servicio.verificarSujetoCredito(cedula);
        vista.evaluacion(e, e.isSujetoCredito() ? servicio.calcularMontoMaximo(cedula) : null);
    }

    private void vender(boolean aCredito) {
        String cedula = vista.pedir("Cedula del cliente");
        String nombre = vista.pedir("Nombre (solo si es cliente nuevo)");
        Integer cod = vista.pedirEntero("Codigo del celular");
        if (cedula == null || nombre == null || cod == null) {
            return;
        }
        ResultadoFactura r;
        if (aCredito) {
            Integer plazo = vista.pedirEntero("Plazo en meses (3 a 18)");
            if (plazo == null) {
                return;
            }
            r = servicio.facturarCredito(cedula, nombre, cod, plazo);
        } else {
            r = servicio.facturarEfectivo(cedula, nombre, cod);
        }
        vista.mensaje(r.getMensaje());
        if (r.isExito() && r.getFactura() != null) {
            vista.factura(r.getFactura());
            if (aCredito) {
                vista.amortizacion(servicio.obtenerAmortizacion(r.getFactura().getCodCredito()));
            }
        }
    }

    private void consultarFacturas() {
        String cedula = vista.pedir("Cedula");
        if (cedula != null) {
            vista.facturas(servicio.consultarFacturas(cedula));
        }
    }

    private void verAmortizacion() {
        Integer cod = vista.pedirEntero("Codigo del credito");
        if (cod != null) {
            vista.amortizacion(servicio.obtenerAmortizacion(cod));
        }
    }
}
