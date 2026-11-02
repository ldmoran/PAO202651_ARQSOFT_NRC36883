namespace ec.edu.monster.ClienteConsola.Modelos
{
    /// <summary>Estado de la sesion del usuario: guarda el token que entrego el servidor.</summary>
    public class Sesion
    {
        private readonly object candado = new();
        private string? token;

        public void Iniciar(string nuevoToken) { lock (candado) { token = nuevoToken; } }
        public void Cerrar() { lock (candado) { token = null; } }
        public bool Activa { get { lock (candado) { return token != null; } } }
        public string? Token { get { lock (candado) { return token; } } }
    }
}
