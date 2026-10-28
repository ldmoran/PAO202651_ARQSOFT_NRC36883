using System.Data.Common;
using ec.edu.monster.Modelos.Entidades;

namespace ec.edu.monster.Datos
{
    /// <summary>Lee las tablas categoria y unidad (solo las filas activas).</summary>
    public class CatalogoRepositorio
    {
        private const string Sql =
            "SELECT c.id_categoria, c.nombre AS categoria, u.id_unidad, u.nombre AS unidad, u.factor_base, u.desplazamiento " +
            "FROM dbo.categoria c JOIN dbo.unidad u ON u.id_categoria = c.id_categoria " +
            "WHERE c.activa = 1 AND u.activa = 1 ORDER BY c.id_categoria, u.orden";

        private readonly IFuenteDatos fuente;

        public CatalogoRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        /// <summary>Todas las categorias con sus unidades, en el orden definido en la base.</summary>
        public IReadOnlyList<Categoria> Cargar()
        {
            var nombres = new Dictionary<int, string>();
            var unidades = new Dictionary<int, List<Unidad>>();
            var orden = new List<int>();
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = cn.CreateCommand();
                cmd.CommandText = Sql;
                using var rs = cmd.ExecuteReader();
                while (rs.Read())
                {
                    var idCategoria = rs.GetInt32(0);
                    if (nombres.TryAdd(idCategoria, rs.GetString(1)))
                    {
                        unidades[idCategoria] = new List<Unidad>();
                        orden.Add(idCategoria);
                    }
                    unidades[idCategoria].Add(new Unidad(rs.GetInt32(2), rs.GetString(3),
                        Convert.ToDouble(rs.GetDecimal(4)), Convert.ToDouble(rs.GetDecimal(5))));
                }
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo leer el catalogo de unidades", e);
            }
            return orden.Select(id => new Categoria(id, nombres[id], unidades[id])).ToList();
        }
    }
}
