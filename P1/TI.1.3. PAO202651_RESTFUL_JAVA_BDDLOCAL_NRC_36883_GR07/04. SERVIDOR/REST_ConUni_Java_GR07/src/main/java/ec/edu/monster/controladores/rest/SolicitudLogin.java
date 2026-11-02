package ec.edu.monster.controladores.rest;

public class SolicitudLogin {

    private String usuario;
    private String clave;

    public SolicitudLogin() {
    }

    public SolicitudLogin(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
