package ec.edu.monster.consola;

import ec.edu.monster.cliente.EurekaSoap;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente de consola de EurekaBank (Java). Menu numerado: iniciar sesion, ver cuentas, ver movimientos y operar.
 * Direccion del servicio: primer argumento o variable EUREKA_URL.
 */
public class Main {

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getenv("EUREKA_URL");
        if (url == null || url.isBlank()) {
            url = EurekaSoap.URL_POR_DEFECTO;
        }
        new Main(new EurekaSoap(url), new Scanner(System.in, "UTF-8"), System.out).ejecutar();
    }

    private final EurekaSoap servicio;
    private final Scanner entrada;
    private final PrintStream salida;
    private String usuario;

    Main(EurekaSoap servicio, Scanner entrada, PrintStream salida) {
        this.servicio = servicio;
        this.entrada = entrada;
        this.salida = salida;
    }

    void ejecutar() {
        marco("EUREKABANK", "Cliente de consola (Java)", "Servicio: " + servicio.url());
        boolean seguir = true;
        while (seguir) {
            salida.println();
            salida.println("---- Menu | Sesion: " + (usuario == null ? "no iniciada" : usuario) + " ----");
            if (usuario == null) {
                salida.println("  1. Iniciar sesion");
            } else {
                salida.println("  2. Ver cuentas");
                salida.println("  3. Ver movimientos");
                salida.println("  4. Deposito");
                salida.println("  5. Retiro");
                salida.println("  6. Transferencia");
                salida.println("  7. Cerrar sesion");
            }
            salida.println("  0. Salir");
            String op = leer("Opcion: ");
            if (op == null) {
                break;
            }
            if (!op.equals("0") && !op.equals("1") && usuario == null) {
                salida.println("  Debe iniciar sesion primero.");
                continue;
            }
            try {
                switch (op) {
                    case "1" -> iniciarSesion();
                    case "2" -> verCuentas();
                    case "3" -> verMovimientos();
                    case "4" -> operar("DEPOSITO", false);
                    case "5" -> operar("RETIRO", false);
                    case "6" -> operar("TRANSFERENCIA", true);
                    case "7" -> {
                        usuario = null;
                        salida.println("  Sesion cerrada.");
                    }
                    case "0" -> seguir = false;
                    default -> salida.println("  Opcion no valida.");
                }
            } catch (EurekaSoap.ServicioNoDisponible e) {
                salida.println("  " + e.getMessage());
            }
        }
        salida.println("Hasta luego.");
    }

    private void iniciarSesion() {
        String u = leer("Usuario: ");
        String c = leer("Clave: ");
        if (servicio.iniciarSesion(u == null ? "" : u, c == null ? "" : c)) {
            usuario = u.trim();
            salida.println("  Bienvenido, " + usuario + ".");
        } else {
            salida.println("  Usuario o clave incorrectos.");
        }
    }

    private void verCuentas() {
        salida.printf("  %-10s%-32s%-9s%12s%n", "Cuenta", "Cliente", "Moneda", "Saldo");
        for (EurekaSoap.Cuenta c : servicio.cuentas()) {
            salida.printf("  %-10s%-32s%-9s%12s%n", c.codigo, c.nombreCliente, c.moneda, EurekaSoap.dinero(c.saldo));
        }
    }

    private void verMovimientos() {
        String cuenta = leer("Cuenta (vacio = todas): ");
        List<EurekaSoap.Movimiento> lista = servicio.movimientos(cuenta == null ? "" : cuenta);
        salida.printf("  %-10s%4s %-11s%-24s%11s%12s%n", "Cuenta", "N.", "Fecha", "Tipo", "Importe", "Saldo");
        for (EurekaSoap.Movimiento m : lista) {
            salida.printf("  %-10s%4d %-11s%-24s%11s%12s%n", m.codigoCuenta, m.numero, m.fecha, m.tipo,
                    EurekaSoap.dinero(m.importe), EurekaSoap.dinero(m.saldoActual));
        }
    }

    private void operar(String tipo, boolean conDestino) {
        String origen = leer(conDestino ? "Cuenta origen: " : "Cuenta: ");
        String destino = conDestino ? leer("Cuenta destino: ") : null;
        String txt = leer("Importe: ");
        double importe;
        try {
            importe = Double.parseDouble((txt == null ? "" : txt).replace(',', '.'));
        } catch (NumberFormatException e) {
            importe = 0;
        }
        if (importe <= 0) {
            salida.println("  El importe debe ser un numero mayor a 0.");
            return;
        }
        EurekaSoap.Resultado r = servicio.registrar(tipo, origen == null ? "" : origen.trim(), destino == null ? null : destino.trim(), importe);
        salida.println("  " + (r.ok ? "OK: " : "ERROR: ") + r.mensaje);
    }

    private String leer(String pregunta) {
        salida.print(pregunta);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    private void marco(String... lineas) {
        String borde = "+" + "-".repeat(54) + "+";
        salida.println(borde);
        for (String l : lineas) {
            int libre = Math.max(0, 54 - l.length());
            salida.println("|" + " ".repeat(libre / 2) + l + " ".repeat(libre - libre / 2) + "|");
        }
        salida.println(borde);
    }
}
