package ec.edu.monster.paises;

import ec.edu.monster.paises.controlador.PaisController;
import ec.edu.monster.paises.modelo.Conexion;
import ec.edu.monster.paises.modelo.ConsumoAPI;
import ec.edu.monster.paises.modelo.PaisDAO;
import ec.edu.monster.paises.vista.ServidorWeb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/** Arranque de demostracion: base H2 en memoria con el script real de tablas, para probar las interfaces sin MySQL. */
public class DemoConH2 {
    public static void main(String[] args) throws Exception {
        Conexion conexion = new Conexion("jdbc:h2:mem:demo;MODE=MySQL;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        StringBuilder sql = new StringBuilder();
        for (String ln : Files.readAllLines(Path.of("..", "..", "03. BDD", "02_CrearTablas.sql"))) {
            String t = ln.trim().toUpperCase();
            if (t.startsWith("CREATE DATABASE") || t.startsWith("USE ") || t.startsWith("--")) continue;
            sql.append(ln.replaceAll("--.*$", "")).append('\n');
        }
        try (Connection c = conexion.abrir(); Statement st = c.createStatement()) {
            for (String s : sql.toString().split(";")) if (!s.isBlank()) st.execute(s);
        }
        PaisController controlador = new PaisController(new PaisDAO(conexion), new ConsumoAPI());
        if (args.length > 1 && args[1].equals("importar")) {
            System.out.println(controlador.importarPaises());
        }
        int puerto = new ServidorWeb(controlador).iniciar("0.0.0.0", args.length > 0 ? Integer.parseInt(args[0]) : 8090);
        System.out.println("DEMO en http://localhost:" + puerto + "/");
    }
}
