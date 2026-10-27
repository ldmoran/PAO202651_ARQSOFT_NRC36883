package ec.edu.monster.banco.modelos;

import java.time.LocalDate;

/** Cliente del banco (tabla CLIENTE del Core). Es un modelo interno: no viaja por SOAP. */
public class Cliente {

    private final int codCliente;
    private final String cedula;
    private final String nombre;
    private final String genero;
    private final LocalDate fechaNacimiento;

    public Cliente(int codCliente, String cedula, String nombre, String genero, LocalDate fechaNacimiento) {
        this.codCliente = codCliente;
        this.cedula = cedula;
        this.nombre = nombre;
        this.genero = genero;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getCodCliente() {
        return codCliente;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public boolean esMasculino() {
        return "M".equalsIgnoreCase(genero);
    }
}
