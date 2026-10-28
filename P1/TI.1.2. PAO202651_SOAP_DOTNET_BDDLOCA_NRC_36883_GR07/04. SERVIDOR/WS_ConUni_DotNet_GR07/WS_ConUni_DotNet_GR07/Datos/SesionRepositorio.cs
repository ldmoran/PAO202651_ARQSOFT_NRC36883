using System.Data;
using System.Data.Common;

namespace ec.edu.monster.Datos
{
    /// <summary>Estado de una sesion guardada.</summary>
    public record SesionGuardada(long Id, DateTime Expiracion, bool Activa);

    /// <summary>Lee y escribe la tabla sesion (los tokens emitidos al iniciar sesion).</summary>
    public class SesionRepositorio
    {
        private readonly IFuenteDatos fuente;

        public SesionRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        public void Crear(string token, int idUsuario, DateTime emision, DateTime expiracion)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn,
                    "INSERT INTO dbo.sesion (token, id_usuario, fecha_emision, fecha_expiracion, activa) " +
                    "VALUES (@token, @usuario, @emision, @expiracion, 1)");
                cmd.Parametro("@token", token, DbType.AnsiStringFixedLength);
                cmd.Parametro("@usuario", idUsuario, DbType.Int32);
                cmd.Parametro("@emision", emision, DbType.DateTime2);
                cmd.Parametro("@expiracion", expiracion, DbType.DateTime2);
                cmd.ExecuteNonQuery();
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo guardar la sesion", e);
            }
        }

        public SesionGuardada? Buscar(string token)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn,
                    "SELECT id_sesion, fecha_expiracion, activa FROM dbo.sesion WHERE token = @token");
                cmd.Parametro("@token", token, DbType.AnsiString);
                using var rs = cmd.ExecuteReader();
                return rs.Read() ? new SesionGuardada(rs.GetInt64(0), rs.GetDateTime(1), rs.GetBoolean(2)) : null;
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo consultar la sesion", e);
            }
        }

        /// <summary>Marca la sesion como no activa (por ejemplo, cuando se detecta que expiro).</summary>
        public void Desactivar(string token)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn, "UPDATE dbo.sesion SET activa = 0 WHERE token = @token");
                cmd.Parametro("@token", token, DbType.AnsiString);
                cmd.ExecuteNonQuery();
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo cerrar la sesion", e);
            }
        }
    }
}
