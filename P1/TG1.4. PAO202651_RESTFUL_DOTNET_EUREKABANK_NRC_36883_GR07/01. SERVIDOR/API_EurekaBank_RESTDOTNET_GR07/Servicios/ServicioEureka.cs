using System.Security.Cryptography;
using System.Text;
using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Modelos;

namespace ec.edu.monster.eureka.Servicios
{
    /// <summary>Hash de claves: SHA-256 de (sal + clave) en hexadecimal minuscula.</summary>
    public static class Claves
    {
        public static string Hash(string sal, string clave) =>
            Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(sal + clave))).ToLowerInvariant();

        public static bool Coincide(string sal, string clave, string hashGuardado) =>
            CryptographicOperations.FixedTimeEquals(Encoding.UTF8.GetBytes(Hash(sal, clave)), Encoding.UTF8.GetBytes(hashGuardado));
    }

    /// <summary>Reglas de EurekaBank: validacion de datos, inicio de sesion y operaciones de dinero.</summary>
    public class ServicioEureka
    {
        public static readonly string[] TiposPermitidos = { "DEPOSITO", "RETIRO", "TRANSFERENCIA" };
        public const string EmpleadoPorDefecto = "0001";

        private readonly EurekaRepositorio repo;

        public ServicioEureka(EurekaRepositorio repo) => this.repo = repo;

        public bool IniciarSesion(string? usuario, string? contrasena)
        {
            if (string.IsNullOrWhiteSpace(usuario) || string.IsNullOrEmpty(contrasena)) return false;
            var u = repo.BuscarUsuario(usuario.Trim());
            var ok = u is not null && u.Estado == "ACTIVO" && Claves.Coincide(u.Sal, contrasena, u.ClaveHash);
            repo.RegistrarIntento(usuario.Trim(), ok);
            return ok;
        }

        public List<DatosCuenta> Cuentas() => repo.CuentasConClientes();

        public List<MovimientoData> Movimientos(string? cuenta) => repo.Movimientos(cuenta);

        public ResultadoOperacion Registrar(string? origen, string? destino, string? tipo, double importe)
        {
            if (string.IsNullOrWhiteSpace(origen)) return ResultadoOperacion.Error("Cuenta origen es requerida");
            if (double.IsNaN(importe) || importe <= 0) return ResultadoOperacion.Error("El importe debe ser mayor a 0");
            if (string.IsNullOrWhiteSpace(tipo)) return ResultadoOperacion.Error("Tipo de movimiento es requerido");
            var monto = Math.Round((decimal)importe, 2, MidpointRounding.AwayFromZero);
            if (monto <= 0) return ResultadoOperacion.Error("El importe debe ser mayor a 0");
            var o = origen.Trim();
            var hayDestino = !string.IsNullOrWhiteSpace(destino);
            try
            {
                switch (tipo.Trim().ToUpperInvariant())
                {
                    case "DEPOSITO":
                        if (hayDestino) return ResultadoOperacion.Error("Depósito no debe tener cuenta destino");
                        repo.Depositar(o, monto, EmpleadoPorDefecto);
                        break;
                    case "RETIRO":
                        if (hayDestino) return ResultadoOperacion.Error("Retiro no debe tener cuenta destino");
                        repo.Retirar(o, monto, EmpleadoPorDefecto);
                        break;
                    case "TRANSFERENCIA":
                        if (!hayDestino) return ResultadoOperacion.Error("Cuenta destino es requerida para transferencia");
                        repo.Transferir(o, destino!.Trim(), monto, EmpleadoPorDefecto);
                        break;
                    default:
                        return ResultadoOperacion.Error("Tipo de movimiento no válido: " + tipo);
                }
                return ResultadoOperacion.Exito();
            }
            catch (ReglaNegocio e)
            {
                return ResultadoOperacion.Error(e.Message);
            }
            catch (Exception)
            {
                return ResultadoOperacion.Error("Error interno del servidor");
            }
        }
    }
}
