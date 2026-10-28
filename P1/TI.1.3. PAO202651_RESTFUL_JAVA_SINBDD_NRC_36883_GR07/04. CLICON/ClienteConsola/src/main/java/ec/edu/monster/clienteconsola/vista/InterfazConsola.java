package ec.edu.monster.clienteconsola.vista;

import ec.edu.monster.clienteconsola.controlador.ControladorSesion;

/** Menu principal de opciones numericas: reparte el trabajo a las vistas de sesion y de conversion. */
public class InterfazConsola {

    private final ControladorSesion sesion;
    private final VistaSesion vistaSesion;
    private final VistaConversion vistaConversion;
    private final ConsolaIO io;

    public InterfazConsola(ControladorSesion sesion, VistaSesion vistaSesion, VistaConversion vistaConversion, ConsolaIO io) {
        this.sesion = sesion;
        this.vistaSesion = vistaSesion;
        this.vistaConversion = vistaConversion;
        this.io = io;
    }

    public void ejecutar() {
        io.linea();
        io.marco("CONVERSIÓN DE UNIDADES MONSTER", "Cliente de consola", "Servidor: " + sesion.getUrl());
        boolean seguir = true;
        while (seguir) {
            mostrarMenu();
            String op = io.leer("Opción: ");
            if (op == null) {
                break; // fin de la entrada
            }
            switch (op.trim()) {
                case "1":
                    vistaSesion.iniciarSesion();
                    break;
                case "2":
                    if (exigirSesion()) {
                        vistaConversion.verCatalogo();
                    }
                    break;
                case "3":
                    if (exigirSesion()) {
                        vistaConversion.convertir();
                    }
                    break;
                case "4":
                    vistaSesion.cerrarSesion();
                    break;
                case "0":
                    seguir = false;
                    break;
                default:
                    io.linea("  Opción no válida.");
            }
        }
        io.linea("Hasta luego.");
    }

    private void mostrarMenu() {
        io.seccion("Menú  |  Sesión: " + (sesion.haySesion() ? "ACTIVA" : "no iniciada"));
        if (!sesion.haySesion()) {
            io.linea("  1. Iniciar sesión");
        } else {
            io.linea("  2. Ver catálogo (categorías y unidades)");
            io.linea("  3. Convertir");
            io.linea("  4. Cerrar sesión");
        }
        io.linea("  0. Salir");
    }

    private boolean exigirSesion() {
        if (!sesion.haySesion()) {
            io.linea("  Debe iniciar sesión primero.");
            return false;
        }
        return true;
    }
}
