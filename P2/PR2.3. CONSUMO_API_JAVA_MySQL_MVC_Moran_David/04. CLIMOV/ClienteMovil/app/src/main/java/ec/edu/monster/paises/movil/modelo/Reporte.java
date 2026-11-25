package ec.edu.monster.paises.movil.modelo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Resultado del reporte: paises por region, promedio de poblacion y pais mas poblado. */
public class Reporte {
    public static class FilaRegion {
        public String region;
        public int paises;
        public long poblacionTotal;
        public long promedio;
    }

    public final List<FilaRegion> porRegion = new ArrayList<>();
    public Pais masPoblado;
    public int total;

    public static Reporte desdeJson(JSONObject o) {
        Reporte r = new Reporte();
        r.total = o.optInt("total");
        JSONArray filas = o.optJSONArray("porRegion");
        if (filas != null) {
            for (int i = 0; i < filas.length(); i++) {
                JSONObject f = filas.optJSONObject(i);
                if (f == null) continue;
                FilaRegion x = new FilaRegion();
                x.region = f.optString("region", "");
                x.paises = f.optInt("paises");
                x.poblacionTotal = f.optLong("poblacionTotal");
                x.promedio = f.optLong("promedio");
                r.porRegion.add(x);
            }
        }
        JSONObject mas = o.optJSONObject("masPoblado");
        if (mas != null) r.masPoblado = Pais.desdeJson(mas);
        return r;
    }
}
