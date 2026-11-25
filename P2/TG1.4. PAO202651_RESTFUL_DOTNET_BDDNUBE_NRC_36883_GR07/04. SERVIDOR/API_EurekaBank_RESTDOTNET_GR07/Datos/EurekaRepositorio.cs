using System.Data;
using System.Data.Common;
using ec.edu.monster.eureka.Modelos;

namespace ec.edu.monster.eureka.Datos
{
    /// <summary>Todo el SQL de EurekaBank. Las operaciones que mueven dinero corren en una sola transaccion.</summary>
    public class EurekaRepositorio
    {
        public record Credencial(string Usuario, string CodEmpleado, string Sal, string ClaveHash, string Estado);

        private readonly IFuenteDatos fuente;

        public EurekaRepositorio(IFuenteDatos fuente) => this.fuente = fuente;

        private static DbCommand Cmd(DbConnection cn, string sql, DbTransaction? tx = null, params (string, object?)[] ps)
        {
            var c = cn.CreateCommand();
            c.CommandText = sql;
            c.Transaction = tx;
            foreach (var (n, v) in ps)
            {
                var p = c.CreateParameter();
                p.ParameterName = n;
                p.Value = v ?? DBNull.Value;
                c.Parameters.Add(p);
            }
            return c;
        }

        public Credencial? BuscarUsuario(string usuario)
        {
            using var cn = fuente.Abrir();
            using var c = Cmd(cn, "SELECT usuario, cod_empleado, sal, clave_hash, estado FROM usuario WHERE usuario = @u", null, ("@u", usuario));
            using var r = c.ExecuteReader();
            return r.Read() ? new Credencial(r.GetString(0), r.GetString(1).Trim(), r.GetString(2).Trim(), r.GetString(3).Trim(), r.GetString(4)) : null;
        }

        public void RegistrarIntento(string usuario, bool exitoso)
        {
            try
            {
                using var cn = fuente.Abrir();
                using var c = Cmd(cn, "INSERT INTO intento_acceso (usuario, exitoso) VALUES (@u, @e)", null, ("@u", usuario), ("@e", exitoso));
                c.ExecuteNonQuery();
            }
            catch (DbException) { /* el registro de intentos no debe impedir el inicio de sesion */ }
        }

        public List<DatosCuenta> CuentasConClientes()
        {
            const string sql = @"SELECT c.cod_cuenta, m.descripcion, c.saldo, c.estado, cl.apellido_paterno, cl.apellido_materno,
                cl.nombre, cl.email, cl.telefono FROM cuenta c JOIN cliente cl ON cl.cod_cliente = c.cod_cliente
                JOIN moneda m ON m.cod_moneda = c.cod_moneda WHERE c.estado = 'ACTIVO' ORDER BY c.cod_cuenta";
            var lista = new List<DatosCuenta>();
            using var cn = fuente.Abrir();
            using var c = Cmd(cn, sql);
            using var r = c.ExecuteReader();
            while (r.Read())
            {
                lista.Add(new DatosCuenta
                {
                    Codigo = r.GetString(0).Trim(),
                    Moneda = r.GetString(1),
                    Saldo = r.GetDecimal(2),
                    Estado = r.GetString(3),
                    NombreCliente = $"{r.GetString(4)} {r.GetString(5)}, {r.GetString(6)}".Trim(),
                    EmailCliente = r.IsDBNull(7) ? null : r.GetString(7),
                    TelefonoCliente = r.IsDBNull(8) ? null : r.GetString(8)
                });
            }
            return lista;
        }

        public List<MovimientoData> Movimientos(string? cuenta)
        {
            var filtrar = !string.IsNullOrWhiteSpace(cuenta);
            var sql = @"SELECT mv.cod_cuenta, mv.numero, mv.fecha, t.descripcion, t.accion, mv.importe, mv.saldo_resultante,
                mv.cuenta_referencia, cl.apellido_paterno, cl.apellido_materno, cl.nombre FROM movimiento mv
                JOIN tipo_movimiento t ON t.cod_tipo = mv.cod_tipo JOIN cuenta c ON c.cod_cuenta = mv.cod_cuenta
                JOIN cliente cl ON cl.cod_cliente = c.cod_cliente " + (filtrar ? "WHERE mv.cod_cuenta = @c " : "")
                + "ORDER BY mv.fecha DESC, mv.cod_cuenta, mv.numero DESC";
            var lista = new List<MovimientoData>();
            using var cn = fuente.Abrir();
            using var cmd = filtrar ? Cmd(cn, sql, null, ("@c", cuenta!.Trim())) : Cmd(cn, sql);
            using var r = cmd.ExecuteReader();
            while (r.Read())
            {
                lista.Add(new MovimientoData
                {
                    CodigoCuenta = r.GetString(0).Trim(),
                    Numero = r.GetInt32(1),
                    Fecha = r.GetDateTime(2).ToString("yyyy-MM-dd"),
                    Tipo = r.GetString(3) + (r.GetString(4) == "INGRESO" ? " Entrada" : " Salida"),
                    Importe = r.GetDecimal(5),
                    SaldoActual = r.GetDecimal(6),
                    Referencia = r.IsDBNull(7) ? null : r.GetString(7).Trim(),
                    NombreCliente = $"{r.GetString(8)} {r.GetString(9)}, {r.GetString(10)}".Trim()
                });
            }
            return lista;
        }

        public void Depositar(string cuenta, decimal importe, string empleado) =>
            Mover(new[] { new Paso(cuenta, "003", true, null) }, importe, empleado);

        public void Retirar(string cuenta, decimal importe, string empleado) =>
            Mover(new[] { new Paso(cuenta, "004", false, null) }, importe, empleado);

        public void Transferir(string origen, string destino, decimal importe, string empleado)
        {
            if (origen == destino) throw new ReglaNegocio("No se puede transferir a la misma cuenta.");
            Mover(new[] { new Paso(origen, "009", false, destino), new Paso(destino, "008", true, origen) }, importe, empleado);
        }

        private record Paso(string Cuenta, string Tipo, bool Ingreso, string? Referencia);

        /// <summary>Aplica todos los pasos o ninguno.</summary>
        private void Mover(IEnumerable<Paso> pasos, decimal importe, string empleado)
        {
            using var cn = fuente.Abrir();
            using var tx = cn.BeginTransaction();
            try
            {
                using (var c = Cmd(cn, "SELECT 1 FROM usuario WHERE cod_empleado = @e AND estado = 'ACTIVO'", tx, ("@e", empleado)))
                {
                    if (c.ExecuteScalar() is null) throw new ReglaNegocio($"El empleado {empleado} no existe o no esta activo.");
                }
                foreach (var p in pasos) Aplicar(cn, tx, p, importe, empleado);
                tx.Commit();
            }
            catch
            {
                tx.Rollback();
                throw;
            }
        }

        private static void Aplicar(DbConnection cn, DbTransaction tx, Paso p, decimal importe, string empleado)
        {
            decimal saldo;
            int contador;
            using (var c = Cmd(cn, "SELECT saldo, contador_mov, estado FROM cuenta WITH (UPDLOCK, ROWLOCK) WHERE cod_cuenta = @c", tx, ("@c", p.Cuenta)))
            using (var r = c.ExecuteReader())
            {
                if (!r.Read()) throw new ReglaNegocio($"La cuenta {p.Cuenta} no existe.");
                if (r.GetString(2) != "ACTIVO") throw new ReglaNegocio($"La cuenta {p.Cuenta} no esta activa.");
                saldo = r.GetDecimal(0);
                contador = r.GetInt32(1);
            }
            if (!p.Ingreso && saldo < importe) throw new ReglaNegocio($"Saldo insuficiente en la cuenta {p.Cuenta}.");
            var nuevo = p.Ingreso ? saldo + importe : saldo - importe;
            using (var c = Cmd(cn, "UPDATE cuenta SET saldo = @s, contador_mov = @n WHERE cod_cuenta = @c", tx,
                ("@s", nuevo), ("@n", contador + 1), ("@c", p.Cuenta)))
            {
                c.ExecuteNonQuery();
            }
            using (var c = Cmd(cn, @"INSERT INTO movimiento (cod_cuenta, numero, fecha, cod_empleado, cod_tipo, importe, saldo_resultante, cuenta_referencia)
                VALUES (@c, @n, CAST(GETDATE() AS DATE), @e, @t, @i, @s, @r)", tx,
                ("@c", p.Cuenta), ("@n", contador + 1), ("@e", empleado), ("@t", p.Tipo), ("@i", importe), ("@s", nuevo), ("@r", p.Referencia)))
            {
                c.ExecuteNonQuery();
            }
        }
    }
}
