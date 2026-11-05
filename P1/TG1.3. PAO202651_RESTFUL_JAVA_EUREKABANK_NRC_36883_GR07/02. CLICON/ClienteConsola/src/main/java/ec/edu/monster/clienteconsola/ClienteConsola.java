package ec.edu.monster.clienteconsola;

import ec.edu.monster.clienteconsola.modelo.Cuenta;
import ec.edu.monster.clienteconsola.modelo.ErrorServicio;
import ec.edu.monster.clienteconsola.modelo.Movimiento;
import ec.edu.monster.clienteconsola.modelo.Resultado;
import ec.edu.monster.clienteconsola.modelo.RestClient;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente de consola de EurekaBank: menu de opciones numericas. No contiene reglas de negocio:
 * todo lo hace el servidor REST. URL del servidor: argumento 1, propiedad -Deureka.url, variable
 * EUREKA_URL o http://localhost:8080/REST_EurekaBank_Java_GR07/api/
 */
public class ClienteConsola {

    private final RestClient cliente;
    private final Scanner entrada;
    private final PrintStream salida;
    private boolean conSesion;

    public ClienteConsola(RestClient cliente, Scanner entrada, PrintStream salida) {
        this.cliente = cliente;
        this.entrada = entrada;
        this.salida = salida;
    }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : RestClient.urlConfigurada();
        PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new ClienteConsola(new RestClient(url), new Scanner(System.in, StandardCharsets.UTF_8), salida).ejecutar();
    }

    public void ejecutar() {
        salida.println("+------------------------------------------------------+");
        salida.println("|                     EUREKABANK                       |");
        salida.println("|                  Cliente de consola                  |");
        salida.println("+------------------------------------------------------+");
        salida.println("Servidor: " + cliente.getBaseUrl());
        while (true) {
            mostrarMenu();
            String op = leer("Opción");
            if (op == null || op.equals("0")) {
                salida.println("Hasta luego.");
                return;
            }
            try {
                if (!conSesion) {
                    if (op.equals("1")) {
                        iniciarSesion();
                    } else {
                        salida.println("  Opción no válida.");
                    }
                    continue;
                }
                switch (op) {
                    case "2" -> verCuentas();
                    case "3" -> verMovimientos();
                    case "4" -> operar("DEPOSITO");
                    case "5" -> operar("RETIRO");
                    case "6" -> operar("TRANSFERENCIA");
                    case "7" -> {
                        conSesion = false;
                        salida.println("  Sesión cerrada.");
                    }
                    default -> salida.println("  Opción no válida.");
                }
            } catch (ErrorServicio e) {
                salida.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("--- Menú  |  Sesión: " + (conSesion ? "ACTIVA" : "no iniciada") + " " + "-".repeat(20));
        if (!conSesion) {
            salida.println("  1. Iniciar sesión");
        } else {
            salida.println("  2. Ver cuentas");
            salida.println("  3. Ver movimientos de una cuenta");
            salida.println("  4. Depósito");
            salida.println("  5. Retiro");
            salida.println("  6. Transferencia");
            salida.println("  7. Cerrar sesión");
        }
        salida.println("  0. Salir");
    }

    private String leer(String etiqueta) {
        salida.print(etiqueta + ": ");
        salida.flush();
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    private void iniciarSesion() {
        String usuario = leer("Usuario");
        String clave = leer("Contraseña");
        Resultado r = cliente.login(usuario, clave);
        conSesion = r.ok();
        salida.println(r.ok() ? "  [OK] " + r.mensaje() : "  [ERROR] " + r.mensaje());
    }

    private void verCuentas() {
        List<Cuenta> cuentas = cliente.cuentas();
        salida.println(String.format("  %-10s %-34s %-9s %12s", "CUENTA", "CLIENTE", "MONEDA", "SALDO"));
        for (Cuenta c : cuentas) {
            salida.println(String.format("  %-10s %-34s %-9s %12s", c.codigo(), c.nombreCliente(), c.moneda(), formato(c.saldo())));
        }
    }

    private void verMovimientos() {
        String cuenta = leer("Cuenta (vacío = todas)");
        List<Movimiento> lista = cliente.movimientos(cuenta);
        if (lista.isEmpty()) {
            salida.println("  No hay movimientos.");
            return;
        }
        salida.println(String.format("  %-10s %3s %-11s %-22s %11s %12s", "CUENTA", "N°", "FECHA", "TIPO", "IMPORTE", "SALDO"));
        for (Movimiento m : lista) {
            salida.println(String.format("  %-10s %3d %-11s %-22s %11s %12s", m.codigoCuenta(), m.numero(), m.fecha(), m.tipo(),
                    formato(m.importe()), formato(m.saldoActual())));
        }
    }

    private void operar(String tipo) {
        String origen = leer("Cuenta" + (tipo.equals("TRANSFERENCIA") ? " origen" : ""));
        String destino = tipo.equals("TRANSFERENCIA") ? leer("Cuenta destino") : null;
        String texto = leer("Importe");
        double importe;
        try {
            importe = Double.parseDouble(texto == null ? "" : texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            salida.println("  [ERROR] El importe debe ser un número.");
            return;
        }
        Resultado r = cliente.registrar(origen, destino, tipo, importe);
        salida.println(r.ok() ? "  [OK] " + r.mensaje() : "  [ERROR] " + r.mensaje());
    }

    private static String formato(BigDecimal valor) {
        return valor == null ? "" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
