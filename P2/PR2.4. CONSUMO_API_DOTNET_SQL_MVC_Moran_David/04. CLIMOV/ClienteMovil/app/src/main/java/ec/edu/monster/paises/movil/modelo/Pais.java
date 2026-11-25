package ec.edu.monster.paises.movil.modelo;

import org.json.JSONException;
import org.json.JSONObject;

/** Un pais tal como lo entrega y recibe el servidor. */
public class Pais {
    public int id;
    public String codigo = "";
    public String nombre = "";
    public String capital = "";
    public String region = "";
    public String subregion = "";
    public long poblacion;
    public double area;
    public String moneda = "";
    public String bandera = "";

    public static Pais desdeJson(JSONObject o) {
        Pais p = new Pais();
        p.id = o.optInt("id");
        p.codigo = o.optString("codigo", "");
        p.nombre = o.optString("nombre", "");
        p.capital = o.optString("capital", "");
        p.region = o.optString("region", "");
        p.subregion = o.optString("subregion", "");
        p.poblacion = o.optLong("poblacion");
        p.area = o.optDouble("area", 0);
        p.moneda = o.optString("moneda", "");
        p.bandera = o.optString("bandera", "");
        return p;
    }

    public JSONObject aJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("codigo", codigo);
        o.put("nombre", nombre);
        o.put("capital", capital);
        o.put("region", region);
        o.put("subregion", subregion);
        o.put("poblacion", poblacion);
        o.put("area", area);
        o.put("moneda", moneda);
        o.put("bandera", bandera);
        return o;
    }
}
