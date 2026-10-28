using System.Data;
using System.Data.Common;

namespace ec.edu.monster.Datos
{
    /// <summary>Ayuda para armar comandos con parametros (nunca se concatenan valores en el SQL).</summary>
    internal static class Comandos
    {
        public static DbCommand Crear(DbConnection cn, string sql)
        {
            var cmd = cn.CreateCommand();
            cmd.CommandText = sql;
            return cmd;
        }

        public static void Parametro(this DbCommand cmd, string nombre, object? valor, DbType tipo)
        {
            var p = cmd.CreateParameter();
            p.ParameterName = nombre;
            p.DbType = tipo;
            p.Value = valor ?? DBNull.Value;
            cmd.Parameters.Add(p);
        }
    }
}
