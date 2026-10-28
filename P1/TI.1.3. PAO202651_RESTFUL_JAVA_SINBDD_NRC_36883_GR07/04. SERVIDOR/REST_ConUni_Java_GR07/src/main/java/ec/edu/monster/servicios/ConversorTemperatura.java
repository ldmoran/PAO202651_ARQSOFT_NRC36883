package ec.edu.monster.servicios;

import java.util.List;

/**
 * Temperatura: no es lineal. Se pasa primero a celsius y luego a la escala de
 * destino.
 *
 * A celsius: C = (F - 32) x 5/9; C = K - 273,15; C = N x 100/33; C = Re x 5/4.
 * Desde celsius: F = C x 9/5 + 32; K = C + 273,15; N = C x 33/100; Re = C x 4/5.
 */
public class ConversorTemperatura implements Conversor {

    private static final List<String> UNIDADES =
            List.of("celsius", "fahrenheit", "kelvin", "newton", "reaumur");

    @Override
    public String categoria() {
        return "Temperatura";
    }

    @Override
    public List<String> unidades() {
        return UNIDADES;
    }

    @Override
    public double convertir(double valor, String origen, String destino) {
        double celsius = aCelsius(valor, origen);
        return desdeCelsius(celsius, destino);
    }

    private static double aCelsius(double v, String escala) {
        switch (escala) {
            case "fahrenheit":
                return (v - 32.0) * 5.0 / 9.0;
            case "kelvin":
                return v - 273.15;
            case "newton":
                return v * 100.0 / 33.0;
            case "reaumur":
                return v * 5.0 / 4.0;
            default:
                return v;
        }
    }

    private static double desdeCelsius(double c, String escala) {
        switch (escala) {
            case "fahrenheit":
                return c * 9.0 / 5.0 + 32.0;
            case "kelvin":
                return c + 273.15;
            case "newton":
                return c * 33.0 / 100.0;
            case "reaumur":
                return c * 4.0 / 5.0;
            default:
                return c;
        }
    }
}
