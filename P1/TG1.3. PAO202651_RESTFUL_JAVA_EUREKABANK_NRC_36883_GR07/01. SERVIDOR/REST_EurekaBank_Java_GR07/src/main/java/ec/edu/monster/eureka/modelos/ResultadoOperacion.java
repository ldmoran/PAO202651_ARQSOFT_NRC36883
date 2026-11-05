package ec.edu.monster.eureka.modelos;

/**
 * Resultado de registrar un movimiento: codigo 1 = exito, -1 = error, y el mensaje que se muestra tal cual.
 * El estado HTTP se calcula aparte (no viaja en el JSON): 200 exito, 400 datos invalidos, 422 regla de negocio, 500 error interno.
 */
public class ResultadoOperacion {

    private int codigo;
    private String mensaje = "";
    private transient int http = 200;

    public ResultadoOperacion() {
    }

    private ResultadoOperacion(int codigo, String mensaje, int http) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.http = http;
    }

    public static ResultadoOperacion exito() {
        return new ResultadoOperacion(1, "Operación exitosa", 200);
    }

    /** Dato faltante o con formato incorrecto. */
    public static ResultadoOperacion invalido(String mensaje) {
        return new ResultadoOperacion(-1, mensaje, 400);
    }

    /** Regla de negocio que no se cumple (saldo insuficiente, cuenta inexistente...). */
    public static ResultadoOperacion regla(String mensaje) {
        return new ResultadoOperacion(-1, mensaje, 422);
    }

    public static ResultadoOperacion errorInterno() {
        return new ResultadoOperacion(-1, "Error interno del servidor", 500);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /** Estado HTTP que le corresponde (no es una propiedad del JSON). */
    public int estadoHttp() {
        return http;
    }
}
