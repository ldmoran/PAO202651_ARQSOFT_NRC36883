namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Catalogo en memoria de categorias y unidades (CU-02 y CU-03). Para agregar
    /// una categoria nueva (por ejemplo divisas) basta con implementar
    /// <see cref="IConversor"/> y registrarla aqui; el login, la validacion y la
    /// respuesta estructurada no cambian.
    /// </summary>
    public static class CatalogoUnidades
    {
        private static readonly List<IConversor> Conversores = new()
        {
            new ConversorLongitud(),
            new ConversorMasa(),
            new ConversorTemperatura()
        };

        public static void Registrar(IConversor conversor) => Conversores.Add(conversor);

        /// <summary>Nombres de las categorias disponibles.</summary>
        public static IReadOnlyList<string> Categorias() => Conversores.Select(c => c.Categoria).ToList();

        /// <summary>Busca una categoria por nombre (sin distinguir mayusculas ni tildes).</summary>
        public static IConversor? BuscarCategoria(string? nombre)
        {
            var n = Texto.Normalizar(nombre);
            if (string.IsNullOrEmpty(n))
            {
                return null;
            }
            return Conversores.FirstOrDefault(c => Texto.Normalizar(c.Categoria) == n);
        }

        /// <summary>Categoria a la que pertenece una unidad (ya normalizada), si existe.</summary>
        public static IConversor? CategoriaDeUnidad(string? unidadNormalizada) =>
            unidadNormalizada is null ? null : Conversores.FirstOrDefault(c => c.Unidades.Contains(unidadNormalizada));
    }
}
