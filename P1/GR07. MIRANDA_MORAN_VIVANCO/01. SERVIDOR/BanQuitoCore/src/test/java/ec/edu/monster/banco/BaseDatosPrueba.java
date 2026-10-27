package ec.edu.monster.banco;

import ec.edu.monster.banco.db.FuenteDatos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** Base H2 en memoria con el mismo esquema del Core y datos fijos respecto a HOY (2026-06-15). */
public final class BaseDatosPrueba {

    public static final Clock RELOJ = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("UTC"));

    private BaseDatosPrueba() {
    }

    public static FuenteDatos crear(String nombre) throws SQLException {
        String url = "jdbc:h2:mem:" + nombre + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        FuenteDatos fuente = () -> DriverManager.getConnection(url, "sa", "");
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement()) {
            st.execute("CREATE TABLE CLIENTE (COD_CLIENTE INT PRIMARY KEY, CEDULA VARCHAR(100) UNIQUE, NOMBRE VARCHAR(100), "
                    + "GENERO VARCHAR(1), FECHA_NACIMIENTO DATE)");
            st.execute("CREATE TABLE CUENTA (NUM_CUENTA VARCHAR(8) PRIMARY KEY, COD_CLIENTE INT, SALDO DECIMAL(10,2))");
            st.execute("CREATE TABLE MOVIMIENTO (COD_MOVIMIENTO INT AUTO_INCREMENT PRIMARY KEY, NUM_CUENTA VARCHAR(8), "
                    + "TIPO VARCHAR(3), VALOR DECIMAL(10,2), FECHA DATE)");
            st.execute("CREATE TABLE CREDITO (COD_CREDITO INT AUTO_INCREMENT PRIMARY KEY, COD_CLIENTE INT, "
                    + "MONTO_CREDITO DECIMAL(10,2), PLAZO_MESES INT, TASA_ANUAL DECIMAL(5,3), CUOTA_FIJA DECIMAL(10,2), "
                    + "FECHA_INICIO DATE, FECHA_FIN DATE, ESTADO VARCHAR(10), SALDO_PENDIENTE DECIMAL(10,2))");
            st.execute("CREATE TABLE AMORTIZACION (COD_AMORTIZACION INT AUTO_INCREMENT PRIMARY KEY, COD_CREDITO INT, "
                    + "NUM_CUOTA INT, FECHA_PAGO DATE, VALOR_CUOTA DECIMAL(10,2), INTERES_PAGADO DECIMAL(10,2), "
                    + "CAPITAL_PAGADO DECIMAL(10,2), SALDO DECIMAL(10,2))");

            st.execute("INSERT INTO CLIENTE VALUES (1,'0101010101','Ana Mujer','F','1990-03-10')");
            st.execute("INSERT INTO CLIENTE VALUES (2,'0202020202','Juan Joven','M','2005-01-01')");
            st.execute("INSERT INTO CLIENTE VALUES (3,'0303030303','Luis Sin Deposito','M','1985-05-05')");
            st.execute("INSERT INTO CLIENTE VALUES (4,'0404040404','Marta Con Credito','F','1992-07-07')");
            st.execute("INSERT INTO CLIENTE VALUES (5,'0505050505','Pedro Veinticinco','M','2001-06-15')");
            st.execute("INSERT INTO CLIENTE VALUES (6,'0606060606','Sara Joven','F','2007-02-02')");
            for (int i = 1; i <= 6; i++) {
                st.execute("INSERT INTO CUENTA VALUES ('1000000" + i + "'," + i + ",0)");
            }
            // Ana: depositos 1000 y 1000 (promedio 1000), retiros 400 y 400; un deposito viejo de 5000 que no cuenta
            mov(st, 1, "DEP", "1000.00", "2026-06-01");
            mov(st, 1, "DEP", "1000.00", "2026-04-20");
            mov(st, 1, "RET", "400.00", "2026-05-10");
            mov(st, 1, "RET", "400.00", "2026-04-25");
            mov(st, 1, "DEP", "5000.00", "2026-01-01");
            mov(st, 2, "DEP", "900.00", "2026-06-05");
            mov(st, 3, "DEP", "900.00", "2026-04-10");
            mov(st, 4, "DEP", "900.00", "2026-06-05");
            mov(st, 5, "DEP", "600.00", "2026-06-10");
            mov(st, 6, "DEP", "600.00", "2026-06-10");
            st.execute("INSERT INTO CREDITO (COD_CLIENTE, MONTO_CREDITO, PLAZO_MESES, TASA_ANUAL, CUOTA_FIJA, FECHA_INICIO, "
                    + "FECHA_FIN, ESTADO, SALDO_PENDIENTE) VALUES (4, 300, 6, 0.165, 52.00, '2026-05-01', '2026-11-01', 'ACTIVO', 300)");
        }
        return fuente;
    }

    private static void mov(Statement st, int cliente, String tipo, String valor, String fecha) throws SQLException {
        st.execute("INSERT INTO MOVIMIENTO (NUM_CUENTA, TIPO, VALOR, FECHA) VALUES ('1000000" + cliente + "','" + tipo + "',"
                + valor + ",'" + fecha + "')");
    }
}
