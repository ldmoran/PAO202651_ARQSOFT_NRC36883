using ec.edu.monster.Datos;
using ec.edu.monster.Modelos.Entidades;

namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Catalogo de categorias y unidades (CU-02 y CU-03), leido de las tablas categoria y unidad.
    /// Para agregar una categoria nueva (por ejemplo volumen) basta con insertar sus filas en la
    /// base; el login, la validacion y la respuesta estructurada no cambian.
    /// </summary>
    public class CatalogoUnidades
    {
        private readonly IReadOnlyList<Categoria> categorias;

        private CatalogoUnidades(IReadOnlyList<Categoria> categorias) => this.categorias = categorias;

        /// <summary>Lee el catalogo vigente de la base de datos.</summary>
        public static CatalogoUnidades Cargar() => new(new CatalogoRepositorio(BaseDatos.Fuente).Cargar());

        /// <summary>Nombres de las categorias disponibles.</summary>
        public IReadOnlyList<string> Categorias() => categorias.Select(c => c.Nombre).ToList();

        /// <summary>Busca una categoria por nombre (sin distinguir mayusculas ni tildes).</summary>
        public Categoria? BuscarCategoria(string? nombre)
        {
            var n = Texto.Normalizar(nombre);
            if (string.IsNullOrEmpty(n))
            {
                return null;
            }
            return categorias.FirstOrDefault(c => Texto.Normalizar(c.Nombre) == n);
        }

        /// <summary>Categoria a la que pertenece una unidad (ya normalizada), si existe.</summary>
        public Categoria? CategoriaDeUnidad(string? unidadNormalizada) =>
            unidadNormalizada is null ? null : categorias.FirstOrDefault(c => c.Unidades.Any(u => u.Nombre == unidadNormalizada));

        /// <summary>Unidad de una categoria por su nombre normalizado.</summary>
        public static Unidad? UnidadDe(Categoria categoria, string unidadNormalizada) =>
            categoria.Unidades.FirstOrDefault(u => u.Nombre == unidadNormalizada);
    }
}
