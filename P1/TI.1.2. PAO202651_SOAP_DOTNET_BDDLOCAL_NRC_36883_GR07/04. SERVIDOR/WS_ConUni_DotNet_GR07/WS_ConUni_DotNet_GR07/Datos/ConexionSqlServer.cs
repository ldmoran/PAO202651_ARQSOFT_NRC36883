using System.Data.Common;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.Datos
{
    /// <summary>
    /// Conexion a SQL Server (base "conversion_unidades"). La cadena de conexion no esta en el codigo:
    /// se toma, en este orden, de la variable de entorno CONUNI_DB_CONEXION y de
    /// ConnectionStrings:ConversionUnidades en appsettings.json (autenticacion de Windows por defecto).
    /// </summary>
    public class ConexionSqlServer : IFuenteDatos
    {
        public const string VariableConexion = "CONUNI_DB_CONEXION";
        public const string Llave = "ConversionUnidades";

        private readonly string cadena;

        public ConexionSqlServer(string cadenaPorDefecto)
        {
            var delEntorno = Environment.GetEnvironmentVariable(VariableConexion);
            cadena = string.IsNullOrWhiteSpace(delEntorno) ? cadenaPorDefecto : delEntorno;
        }

        public DbConnection Abrir()
        {
            var conexion = new SqlConnection(cadena);
            conexion.Open();
            return conexion;
        }
    }
}
