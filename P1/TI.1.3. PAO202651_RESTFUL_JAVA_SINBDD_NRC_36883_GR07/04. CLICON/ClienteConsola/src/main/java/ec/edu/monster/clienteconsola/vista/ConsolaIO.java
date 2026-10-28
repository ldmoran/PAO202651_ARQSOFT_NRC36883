package ec.edu.monster.clienteconsola.vista;

import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/** Entrada y salida de texto compartida por las vistas de consola (marcos, listas, lectura validada). */
public class ConsolaIO {

    private static final int ANCHO = 54;

    private final Scanner entrada;
    private final PrintStream salida;

    public ConsolaIO(Scanner entrada, PrintStream salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void linea(String texto) {
        salida.println(texto);
    }

    public void linea() {
        salida.println();
    }

    /** Marco con titulo(s) centrados. */
    public void marco(String... lineas) {
        String borde = "+" + "-".repeat(ANCHO) + "+";
        salida.println(borde);
        for (String l : lineas) {
            int libre = Math.max(0, ANCHO - l.length());
            int izq = libre / 2;
            salida.println("|" + " ".repeat(izq) + l + " ".repeat(libre - izq) + "|");
        }
        salida.println(borde);
    }

    /** Subtitulo de seccion. */
    public void seccion(String titulo) {
        salida.println();
        salida.println("--- " + titulo + " " + "-".repeat(Math.max(3, ANCHO - titulo.length() - 3)));
    }

    public void ok(String texto) {
        salida.println("  [OK] " + texto);
    }

    public void error(RespuestaServicio r) {
        salida.println("  [ERROR " + r.getCodigo() + "] " + r.getMensaje());
        if (r.requiereNuevoLogin()) {
            salida.println("  Su sesión terminó. Inicie sesión nuevamente.");
        }
    }

    public String leer(String texto) {
        salida.print(texto);
        salida.flush();
        return entrada.hasNextLine() ? entrada.nextLine() : null;
    }

    public Double leerNumero(String texto) {
        while (true) {
            String t = leer(texto);
            if (t == null) {
                return null;
            }
            try {
                double v = Double.parseDouble(t.trim().replace(',', '.'));
                if (!Double.isNaN(v) && !Double.isInfinite(v)) {
                    return v;
                }
            } catch (NumberFormatException e) {
                // se vuelve a pedir
            }
            salida.println("  Ingrese un número válido (ejemplo: 12.5).");
        }
    }

    /** Muestra una lista numerada y devuelve el elemento elegido (por numero o por nombre). */
    public String elegir(String titulo, List<String> opciones) {
        seccion(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            salida.println("  " + (i + 1) + ". " + opciones.get(i));
        }
        while (true) {
            String t = leer("Elija (número o nombre, 0 para cancelar): ");
            if (t == null || t.trim().equals("0")) {
                return null;
            }
            t = t.trim();
            try {
                int n = Integer.parseInt(t);
                if (n >= 1 && n <= opciones.size()) {
                    return opciones.get(n - 1);
                }
            } catch (NumberFormatException e) {
                for (String o : opciones) {
                    if (o.equalsIgnoreCase(t)) {
                        return o;
                    }
                }
            }
            salida.println("  Opción no válida.");
        }
    }
}
