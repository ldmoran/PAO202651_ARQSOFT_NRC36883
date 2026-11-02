package ec.edu.monster.clienteconsola.vista;

import ec.edu.monster.clienteconsola.controlador.ControladorConversiones;
import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;

/** Pantallas del catalogo y de la conversion (CU-02, CU-03 y CU-04). */
public class VistaConversion {

    private final ControladorConversiones controlador;
    private final ConsolaIO io;

    public VistaConversion(ControladorConversiones controlador, ConsolaIO io) {
        this.controlador = controlador;
        this.io = io;
    }

    public void verCatalogo() {
        RespuestaServicio cats = controlador.categorias();
        if (!cats.esExitosa()) {
            io.error(cats);
            return;
        }
        io.seccion("Catálogo de unidades");
        for (String c : cats.getDatos()) {
            RespuestaServicio u = controlador.unidades(c);
            if (!u.esExitosa()) {
                io.error(u);
                return;
            }
            io.linea("  " + c + ": " + String.join(", ", u.getDatos()));
        }
    }

    public void convertir() {
        RespuestaServicio cats = controlador.categorias();
        if (!cats.esExitosa()) {
            io.error(cats);
            return;
        }
        String categoria = io.elegir("Categoría", cats.getDatos());
        if (categoria == null) {
            return;
        }
        RespuestaServicio un = controlador.unidades(categoria);
        if (!un.esExitosa()) {
            io.error(un);
            return;
        }
        String origen = io.elegir("Unidad de origen", un.getDatos());
        if (origen == null) {
            return;
        }
        String destino = io.elegir("Unidad de destino", un.getDatos());
        if (destino == null) {
            return;
        }
        Double valor = io.leerNumero("Valor a convertir: ");
        if (valor == null) {
            return;
        }
        RespuestaServicio r = controlador.convertir(categoria, valor, origen, destino);
        if (r.esExitosa() && r.getResultado() != null) {
            io.linea();
            io.marco("RESULTADO",
                    ControladorConversiones.formatear(valor) + " " + r.getUnidadOrigen(),
                    "=",
                    ControladorConversiones.formatear(r.getResultado()) + " " + r.getUnidadDestino());
            io.linea("  " + r.getMensaje());
        } else {
            io.error(r);
        }
    }
}
