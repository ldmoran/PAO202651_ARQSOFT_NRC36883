# Cliente_Web_GR07 - Conversión de Unidades Monster (GR07)

Aplicación web Jakarta Servlets + HTML/JavaScript. Los servlets llaman por REST al servidor de conversiones y mantienen el token en la sesión del servidor web; el navegador no recibe el token Bearer.

## Requisitos y despliegue

- JDK 21 y Maven 3.9+
- Payara 6 / GlassFish 7
- API REST desplegada (ver [README del servidor](../REST_ConUni_Java_GR07/README.md))

```bash
mvn clean test package
```

Despliega `target/Cliente_Web_GR07.war` y abre `http://localhost:8080/Cliente_Web_GR07/`. La URL de la API se configura mediante el parámetro `conuni.url` de `WEB-INF/web.xml`.

## Uso

Inicia sesión con `monster` / `monster9`; el navegador carga categorías y unidades y permite convertir unidades. La sesión web guarda el token en una cookie HttpOnly; al vencer o ser rechazado, el cliente pide iniciar sesión de nuevo.

## Estructura y pruebas

`controlador/` contiene los servlets y el JSON de la interfaz; `modelo/` contiene `RestClient` y la respuesta; `webapp/` contiene las páginas, estilos e imágenes. `mvn test` prueba el cliente HTTP y la serialización sin requerir el servidor real.
