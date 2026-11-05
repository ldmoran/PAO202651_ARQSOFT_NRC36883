package ec.edu.monster.eureka.controladores;

/** Cuerpo JSON de POST /api/sesion/login. */
public class SolicitudLogin {

    private String usuario;
    private String contrasena;

    public SolicitudLogin() {
    }

    public SolicitudLogin(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
