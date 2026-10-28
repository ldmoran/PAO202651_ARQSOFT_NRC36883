using System.Data;
using System.Data.Common;
using ec.edu.monster.Modelos.Entidades;

namespace ec.edu.monster.Datos
{
    /// <summary>Lee la tabla usuario.</summary>
    public class UsuarioRepositorio
    {
        private readonly IFuenteDatos fuente;

        public UsuarioRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        /// <summary>Usuario activo con ese nombre (la base compara sin distinguir mayusculas; el servicio si lo hace).</summary>
        public Usuario? BuscarActivo(string nombre)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn,
                    "SELECT id_usuario, nombre_usuario, sal_clave, hash_clave FROM dbo.usuario " +
                    "WHERE nombre_usuario = @nombre AND activo = 1");
                cmd.Parametro("@nombre", nombre, DbType.AnsiString);
                using var rs = cmd.ExecuteReader();
                return rs.Read() ? new Usuario(rs.GetInt32(0), rs.GetString(1), rs.GetString(2), rs.GetString(3)) : null;
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo consultar el usuario", e);
            }
        }
    }
}
