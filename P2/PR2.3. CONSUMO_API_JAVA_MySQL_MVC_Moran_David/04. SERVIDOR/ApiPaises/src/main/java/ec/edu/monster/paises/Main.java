package ec.edu.monster.paises;

import ec.edu.monster.paises.controlador.PaisController;
import ec.edu.monster.paises.modelo.Conexion;
import ec.edu.monster.paises.modelo.ConsumoAPI;
import ec.edu.monster.paises.modelo.PaisDAO;
import ec.edu.monster.paises.vista.ServidorWeb;

/** Arranque: java -jar ApiPaises.jar [puerto]. La web queda en http://localhost:8090/ y la API en /api. */
public class Main {

    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        PaisController controlador = new PaisController(new PaisDAO(new Conexion()), new ConsumoAPI());
        int real = new ServidorWeb(controlador).iniciar("0.0.0.0", puerto);
        System.out.println("Taller de países en marcha: http://localhost:" + real + "/   (API en /api)");
        for (String ip : direccionesDeEstaPc()) {
            System.out.println("Desde un celular real (misma red wifi): http://" + ip + ":" + real + "/");
        }
        System.out.println("Desde el emulador de Android: http://10.0.2.2:" + real + "/");
    }

    /** Las direcciones IPv4 de este computador en la red local (las que sirven para el celular). */
    private static java.util.List<String> direccionesDeEstaPc() {
        java.util.List<String> r = new java.util.ArrayList<>();
        try {
            for (java.net.NetworkInterface ni : java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (java.net.InetAddress a : java.util.Collections.list(ni.getInetAddresses())) {
                    if (a instanceof java.net.Inet4Address && a.isSiteLocalAddress()) r.add(a.getHostAddress());
                }
            }
        } catch (java.net.SocketException ignorada) {
            // sin red: solo se muestra localhost
        }
        return r;
    }
}
