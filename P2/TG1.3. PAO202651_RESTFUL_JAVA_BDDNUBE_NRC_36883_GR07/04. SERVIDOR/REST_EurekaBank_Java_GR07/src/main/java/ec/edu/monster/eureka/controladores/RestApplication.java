package ec.edu.monster.eureka.controladores;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Todos los recursos REST de EurekaBank cuelgan de /api. */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
