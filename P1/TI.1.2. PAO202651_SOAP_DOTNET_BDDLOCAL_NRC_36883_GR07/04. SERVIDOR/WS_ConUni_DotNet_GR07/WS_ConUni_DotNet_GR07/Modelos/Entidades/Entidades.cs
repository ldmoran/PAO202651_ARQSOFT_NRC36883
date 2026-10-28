namespace ec.edu.monster.Modelos.Entidades
{
    /// <summary>
    /// Unidad de una categoria. Para pasar a la base se usa (valor + desplazamiento) x factor; para
    /// volver, base / factor - desplazamiento.
    /// </summary>
    public record Unidad(int Id, string Nombre, double Factor, double Desplazamiento);

    /// <summary>Categoria con sus unidades (por ejemplo Longitud: centimetros, metros, pies...).</summary>
    public record Categoria(int Id, string Nombre, IReadOnlyList<Unidad> Unidades)
    {
        /// <summary>Nombres de las unidades, en el orden definido en la base.</summary>
        public IReadOnlyList<string> NombresUnidades() => Unidades.Select(u => u.Nombre).ToList();
    }

    /// <summary>Usuario del sistema; la clave se guarda como SHA-256(sal + clave).</summary>
    public record Usuario(int Id, string Nombre, string Sal, string HashClave);
}
