package ec.edu.monster.paises.controlador;

import ec.edu.monster.paises.modelo.ConsumoAPI;
import ec.edu.monster.paises.modelo.ConversorPais;
import ec.edu.monster.paises.modelo.Pais;
import ec.edu.monster.paises.modelo.PaisDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador del patron MVC: coordina el modelo (API, DAO) y entrega resultados a la vista.
 * Aqui viven las reglas: validaciones, importacion sin duplicados y reportes.
 */
public class PaisController {

    public static class ErrorDeNegocio extends Exception {
        public ErrorDeNegocio(String mensaje) {
            super(mensaje);
        }
    }

    private final PaisDAO dao;
    private final ConsumoAPI api;

    public PaisController(PaisDAO dao, ConsumoAPI api) {
        this.dao = dao;
        this.api = api;
    }

    /** Actividad base: consume la API, convierte el JSON y guarda cada pais (nuevo o actualizado). */
    public Map<String, Object> importarPaises() throws IOException, SQLException {
        List<Pais> paises = ConversorPais.desdeJson(api.obtenerTodos());
        if (paises.isEmpty()) {
            throw new IOException("La API no devolvio paises");
        }
        int nuevos = 0;
        int actualizados = 0;
        for (Pais p : paises) {
            if (dao.guardarOActualizar(p)) {
                nuevos++;
            } else {
                actualizados++;
            }
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("recibidos", paises.size());
        r.put("nuevos", nuevos);
        r.put("actualizados", actualizados);
        r.put("total", dao.contar());
        return r;
    }

    /** Actividad 1: consulta un pais por nombre directamente en la API (sin guardarlo). */
    public List<Pais> consultarEnApi(String nombre) throws IOException, ErrorDeNegocio {
        if (nombre == null || nombre.isBlank()) {
            throw new ErrorDeNegocio("Escribe el nombre de un país");
        }
        return ConversorPais.desdeJson(api.obtenerPorNombre(nombre.trim()));
    }

    /** Guarda en la base un pais consultado en la API (por su codigo). */
    public Pais guardarDesdeApi(String nombre, String codigo) throws IOException, SQLException, ErrorDeNegocio {
        for (Pais p : consultarEnApi(nombre)) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                dao.guardarOActualizar(p);
                return dao.listar(null, null, null).stream().filter(x -> x.getCodigo().equals(p.getCodigo())).findFirst().orElse(p);
            }
        }
        throw new ErrorDeNegocio("No se encontró ese país en la API");
    }

    /** Actividades 2 a 4: listado con busqueda por nombre y capital y filtro por region. */
    public List<Pais> listar(String nombre, String capital, String region) throws SQLException {
        return dao.listar(nombre, capital, region);
    }

    public Pais obtener(int id) throws SQLException, ErrorDeNegocio {
        return dao.buscarPorId(id).orElseThrow(() -> new ErrorDeNegocio("No existe el país " + id));
    }

    public Pais crear(Pais p) throws SQLException, ErrorDeNegocio {
        validar(p);
        dao.insertar(p);
        return p;
    }

    public Pais actualizar(int id, Pais p) throws SQLException, ErrorDeNegocio {
        validar(p);
        p.setId(id);
        if (!dao.actualizar(p)) {
            throw new ErrorDeNegocio("No existe el país " + id);
        }
        return p;
    }

    public void eliminar(int id) throws SQLException, ErrorDeNegocio {
        if (!dao.eliminar(id)) {
            throw new ErrorDeNegocio("No existe el país " + id);
        }
    }

    private void validar(Pais p) throws ErrorDeNegocio {
        if (p.getNombre().isEmpty()) {
            throw new ErrorDeNegocio("El nombre es obligatorio");
        }
        if (p.getCodigo().length() != 3 || !p.getCodigo().matches("[A-Z]{3}")) {
            throw new ErrorDeNegocio("El código debe tener 3 letras (por ejemplo ECU)");
        }
        if (p.getRegion().isEmpty()) {
            throw new ErrorDeNegocio("La región es obligatoria");
        }
        if (p.getPoblacion() < 0 || p.getArea() < 0) {
            throw new ErrorDeNegocio("La población y el área no pueden ser negativas");
        }
    }

    // ------------------------------------------------------------------ actividad 5: reportes
    public Map<String, Object> reportes() throws SQLException {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("porRegion", dao.porRegion());
        Optional<Pais> mas = dao.masPoblado();
        r.put("masPoblado", mas.orElse(null));
        r.put("total", dao.contar());
        return r;
    }
}
