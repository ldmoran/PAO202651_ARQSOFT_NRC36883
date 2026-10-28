using System.Data.Common;

namespace ec.edu.monster.Datos
{
    /// <summary>Entrega conexiones abiertas a la base de datos. Las pruebas usan una base temporal.</summary>
    public interface IFuenteDatos
    {
        DbConnection Abrir();
    }
}
