using ec.edu.monster.Datos;
using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;

namespace ec.edu.monster.Seguridad
{
    /// <summary>
    /// CU-01: Autenticarse. Las credenciales viven en la tabla usuario (clave con sal y SHA-256) y
    /// cada intento, correcto o no, queda en la bitacora intento_acceso.
    /// </summary>
    public static class ServicioAutenticacion
    {
        public static RespuestaServicio Autenticar(string? usuario, string? clave)
        {
            if (string.IsNullOrWhiteSpace(usuario) || string.IsNullOrWhiteSpace(clave))
            {
                return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes, "Usuario y clave son obligatorios");
            }
            var nombre = usuario.Trim();
            var encontrado = new UsuarioRepositorio(BaseDatos.Fuente).BuscarActivo(nombre);
            if (encontrado is not null && encontrado.Nombre != nombre)
            {
                encontrado = null; // la base no distingue mayusculas; el servicio si
            }
            var bitacora = new AccesoRepositorio(BaseDatos.Fuente);
            if (encontrado is null)
            {
                bitacora.Registrar(null, nombre, false, "Usuario inexistente", BaseDatos.Ahora());
                return RespuestaMapeador.Error(CodigoResultado.CredencialesIncorrectas, "Usuario o clave incorrectos");
            }
            if (!Claves.Coincide(clave, encontrado.Sal, encontrado.HashClave))
            {
                bitacora.Registrar(encontrado.Id, nombre, false, "Clave incorrecta", BaseDatos.Ahora());
                return RespuestaMapeador.Error(CodigoResultado.CredencialesIncorrectas, "Usuario o clave incorrectos");
            }
            bitacora.Registrar(encontrado.Id, nombre, true, "Inicio de sesion correcto", BaseDatos.Ahora());
            return RespuestaMapeador.ExitoLogin(GestorTokens.GenerarToken(encontrado.Id));
        }
    }
}
