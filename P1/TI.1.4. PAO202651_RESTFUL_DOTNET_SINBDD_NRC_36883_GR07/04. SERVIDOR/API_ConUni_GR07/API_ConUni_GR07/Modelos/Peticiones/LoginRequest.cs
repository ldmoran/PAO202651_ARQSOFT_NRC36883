namespace ec.edu.monster.Modelos.Peticiones
{
    /// <summary>Cuerpo JSON de POST /api/auth/login (CU-01). Exclusivo de la variante REST.</summary>
    public class LoginRequest
    {
        public string? Usuario { get; set; }
        public string? Clave { get; set; }
    }
}
