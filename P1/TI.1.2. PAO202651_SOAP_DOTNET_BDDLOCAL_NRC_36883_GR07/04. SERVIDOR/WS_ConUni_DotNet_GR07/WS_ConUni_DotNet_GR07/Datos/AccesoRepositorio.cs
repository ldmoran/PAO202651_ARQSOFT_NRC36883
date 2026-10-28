using System.Data;
using System.Data.Common;

namespace ec.edu.monster.Datos
{
    /// <summary>Escribe en la bitacora intento_acceso.</summary>
    public class AccesoRepositorio
    {
        private readonly IFuenteDatos fuente;

        public AccesoRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        /// <summary>Registra un intento de inicio de sesion; idUsuario es null si el usuario no existe.</summary>
        public void Registrar(int? idUsuario, string nombreIngresado, bool exitoso, string detalle, DateTime ahora)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn,
                    "INSERT INTO dbo.intento_acceso (id_usuario, nombre_ingresado, exitoso, detalle, fecha_hora) " +
                    "VALUES (@usuario, @nombre, @exitoso, @detalle, @fecha)");
                cmd.Parametro("@usuario", idUsuario, DbType.Int32);
                cmd.Parametro("@nombre", nombreIngresado.Length <= 50 ? nombreIngresado : nombreIngresado[..50], DbType.AnsiString);
                cmd.Parametro("@exitoso", exitoso, DbType.Boolean);
                cmd.Parametro("@detalle", detalle, DbType.AnsiString);
                cmd.Parametro("@fecha", ahora, DbType.DateTime2);
                cmd.ExecuteNonQuery();
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo registrar el intento de acceso", e);
            }
        }
    }
}
