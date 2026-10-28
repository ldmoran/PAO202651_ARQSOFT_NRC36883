using System.Data;
using System.Data.Common;

namespace ec.edu.monster.Datos
{
    /// <summary>Escribe en la tabla conversion (historial de conversiones).</summary>
    public class ConversionRepositorio
    {
        private readonly IFuenteDatos fuente;

        public ConversionRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        /// <summary>Guarda una conversion ligada a la sesion del token.</summary>
        public void Registrar(string token, int idUnidadOrigen, int idUnidadDestino,
            double valorEntrada, double resultado, DateTime ahora)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var cmd = Comandos.Crear(cn,
                    "INSERT INTO dbo.conversion (id_sesion, id_unidad_origen, id_unidad_destino, valor_entrada, resultado, fecha_hora) " +
                    "SELECT id_sesion, @origen, @destino, @valor, @resultado, @fecha FROM dbo.sesion WHERE token = @token");
                cmd.Parametro("@origen", idUnidadOrigen, DbType.Int32);
                cmd.Parametro("@destino", idUnidadDestino, DbType.Int32);
                cmd.Parametro("@valor", valorEntrada, DbType.Double);
                cmd.Parametro("@resultado", resultado, DbType.Double);
                cmd.Parametro("@fecha", ahora, DbType.DateTime2);
                cmd.Parametro("@token", token, DbType.AnsiString);
                cmd.ExecuteNonQuery();
            }
            catch (DbException e)
            {
                throw new ErrorDatos("No se pudo guardar la conversion en el historial", e);
            }
        }
    }
}
