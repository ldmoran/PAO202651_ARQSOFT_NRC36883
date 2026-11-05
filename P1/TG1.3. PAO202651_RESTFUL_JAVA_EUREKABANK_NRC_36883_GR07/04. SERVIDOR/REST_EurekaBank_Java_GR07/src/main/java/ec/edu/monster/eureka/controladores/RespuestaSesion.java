package ec.edu.monster.eureka.controladores;

/** Respuesta de POST /api/sesion/login: resultado true si las credenciales son correctas. */
public class RespuestaSesion {

    private boolean resultado;
    private String mensaje;

    public RespuestaSesion() {
    }

    public RespuestaSesion(boolean resultado, String mensaje) {
        this.resultado = resultado;
        this.mensaje = mensaje;
    }

    public boolean isResultado() {
        return resultado;
    }

    public void setResultado(boolean resultado) {
        this.resultado = resultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
