package ec.edu.monster.paises.modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/** Convierte el JSON de la API publica en objetos Pais. La region se guarda en espanol. */
public final class ConversorPais {

    private ConversorPais() {
    }

    public static List<Pais> desdeJson(String json) {
        List<Pais> lista = new ArrayList<>();
        JsonElement raiz = JsonParser.parseString(json);
        if (!raiz.isJsonArray()) {
            return lista;
        }
        JsonArray arreglo = raiz.getAsJsonArray();
        for (JsonElement e : arreglo) {
            if (e.isJsonObject()) {
                Pais p = convertir(e.getAsJsonObject());
                if (p != null) {
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    static Pais convertir(JsonObject o) {
        String nombre = texto(o, "name");
        String codigo = texto(o, "alpha3Code");
        if (nombre.isEmpty() || codigo.length() != 3) {
            return null;
        }
        Pais p = new Pais();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setCapital(texto(o, "capital"));
        p.setRegion(regionEnEspanol(texto(o, "region")));
        p.setSubregion(subregionEnEspanol(texto(o, "subregion")));
        p.setPoblacion(numero(o, "population"));
        p.setArea(numero(o, "area"));
        p.setMoneda(moneda(o));
        String alfa2 = texto(o, "alpha2Code").toLowerCase();
        if (alfa2.length() == 2) {
            p.setBandera("https://flagcdn.com/w320/" + alfa2 + ".png");   // direccion estable (la API a veces trae enlaces de Wikipedia que fallan)
        } else {
            JsonElement flags = o.get("flags");
            if (flags != null && flags.isJsonObject() && flags.getAsJsonObject().has("png")) {
                p.setBandera(flags.getAsJsonObject().get("png").getAsString());
            }
        }
        return p;
    }

    private static String moneda(JsonObject o) {
        JsonElement m = o.get("currencies");
        if (m != null && m.isJsonArray() && m.getAsJsonArray().size() > 0 && m.getAsJsonArray().get(0).isJsonObject()) {
            JsonObject primera = m.getAsJsonArray().get(0).getAsJsonObject();
            String nombre = texto(primera, "name");
            String codigo = texto(primera, "code");
            return codigo.isEmpty() ? nombre : (nombre.isEmpty() ? codigo : nombre + " (" + codigo + ")");
        }
        return "";
    }

    private static String texto(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return (e == null || e.isJsonNull() || !e.isJsonPrimitive()) ? "" : e.getAsString().trim();
    }

    private static long numero(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        try {
            return (e == null || e.isJsonNull()) ? 0 : (long) e.getAsDouble();
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static String subregionEnEspanol(String s) {
        return switch (s == null ? "" : s.trim()) {
            case "Northern Africa" -> "África del Norte";
            case "Western Africa" -> "África Occidental";
            case "Eastern Africa" -> "África Oriental";
            case "Middle Africa" -> "África Central";
            case "Southern Africa" -> "África Austral";
            case "Northern America" -> "América del Norte";
            case "Central America" -> "América Central";
            case "South America" -> "América del Sur";
            case "Caribbean" -> "Caribe";
            case "Central Asia" -> "Asia Central";
            case "Eastern Asia" -> "Asia Oriental";
            case "South-Eastern Asia" -> "Sudeste Asiático";
            case "Southern Asia" -> "Asia Meridional";
            case "Western Asia" -> "Asia Occidental";
            case "Eastern Europe" -> "Europa Oriental";
            case "Northern Europe" -> "Europa del Norte";
            case "Southern Europe" -> "Europa del Sur";
            case "Western Europe" -> "Europa Occidental";
            case "Central Europe" -> "Europa Central";
            case "Australia and New Zealand" -> "Australia y Nueva Zelanda";
            case "Polynesia" -> "Polinesia";
            default -> s == null ? "" : s.trim();
        };
    }

    /** Americas -> America (con tilde), Africa, Europe, Asia, Oceania, Antarctic/Polar -> Antartida. */
    public static String regionEnEspanol(String region) {
        return switch (region == null ? "" : region.trim().toLowerCase()) {
            case "americas", "america" -> "América";
            case "africa" -> "África";
            case "europe" -> "Europa";
            case "asia" -> "Asia";
            case "oceania" -> "Oceanía";
            case "antarctic", "antarctic ocean", "polar" -> "Antártida";
            default -> region == null || region.isBlank() ? "Sin región" : region.trim();
        };
    }
}
