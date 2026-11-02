# ClienteConsola - Conversión de Unidades Monster (GR07)

Cliente de consola Java del servicio REST `REST_ConUni_Java_GR07`. No calcula unidades localmente: consume la API JSON del servidor.

## Requisitos y ejecución

- JDK 21, Maven 3.9+
- Servidor REST desplegado (ver [README del servidor](../REST_ConUni_Java_GR07/README.md))

```bash
mvn clean test
mvn clean compile dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
java -cp "target/classes;target/dependency/*" ec.edu.monster.clienteconsola.ClienteConsola
```

En Windows, el URL del servidor se puede pasar como primer argumento, por `-Dconuni.url=...` o por `CONUNI_URL`. Por defecto usa `http://localhost:8080/REST_ConUni_Java_GR07/api/`.

## Uso y arquitectura

1. Inicia sesión con `monster` / `monster9`.
2. Con sesión activa consulta las categorías/unidades y ejecuta conversiones.
3. Las llamadas protegidas envían `Authorization: Bearer <token>`; los errores de token cierran la sesión local.

El diseño conserva MVC: `modelo/` implementa el cliente HTTP/JSON, el DTO de respuesta y la sesión; `controlador/` coordina autenticación y conversiones; `vista/` contiene menú y entrada validada.

## Pruebas

`mvn test` ejecuta pruebas contra un servidor HTTP falso; no requiere Payara ni el servidor real.
