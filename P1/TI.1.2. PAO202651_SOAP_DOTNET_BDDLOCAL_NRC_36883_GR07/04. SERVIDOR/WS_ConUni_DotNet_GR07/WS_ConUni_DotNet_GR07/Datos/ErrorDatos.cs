namespace ec.edu.monster.Datos
{
    /// <summary>Falla al hablar con la base de datos. Los manejadores la convierten en el codigo SRV-500.</summary>
    public class ErrorDatos : Exception
    {
        public ErrorDatos(string mensaje, Exception causa) : base(mensaje, causa)
        {
        }
    }
}
