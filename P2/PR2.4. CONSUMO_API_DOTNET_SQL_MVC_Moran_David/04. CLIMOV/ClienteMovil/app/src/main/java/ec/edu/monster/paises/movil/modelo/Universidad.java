package ec.edu.monster.paises.movil.modelo;

import org.json.JSONObject;

/** Una universidad tal como la entrega el servidor .NET. */
public class Universidad {
    public int id;
    public String nombre = "";
    public String dominio = "";
    public String sitioWeb = "";
    public String pais = "";

    public static Universidad desdeJson(JSONObject o) {
        Universidad u = new Universidad();
        u.id = o.optInt("id");
        u.nombre = o.optString("nombre", "");
        u.dominio = o.optString("dominio", "");
        u.sitioWeb = o.optString("sitioWeb", "");
        u.pais = o.optString("pais", "");
        return u;
    }
}
