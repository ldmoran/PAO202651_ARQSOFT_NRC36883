# Cliente_Web_GR07 - Conversión de Unidades Monster (GR07)

Cliente web (Jakarta Servlets + HTML/JavaScript) del servicio SOAP `WS_ConUni_Java_GR07`.
El navegador nunca habla SOAP ni conoce el token: los servlets llaman al servicio SOAP y
guardan el token en la sesión del servidor web.

## Requisitos
- JDK 21 y Maven
- Servidor Jakarta EE 10 (Payara 6 / GlassFish 7) con este WAR y con el servicio SOAP desplegado

## Desplegar
```bash
mvn clean package        # genera target/Cliente_Web_GR07.war
```
Despliega el WAR en Payara (desde NetBeans: Run). Dirección:
`http://localhost:8080/Cliente_Web_GR07/`

URL del servicio SOAP: parámetro `conuni.url` en `src/main/webapp/WEB-INF/web.xml`
(por defecto `http://localhost:8080/WS_ConUni_Java_GR07/`).

## Uso
1. Iniciar sesión (usuario `monster`, clave `monster9`).
2. La página de conversión carga las categorías y unidades del servidor; elegir categoría, unidades,
   escribir el valor (punto o coma decimal) y pulsar "Convertir".
3. Si el token es inválido o la sesión expiró, el cliente avisa y vuelve al login.

## Estructura
- `controlador/`: servlets `LoginServlet`, `SesionServlet`, `CatalogoServlet`, `ConvertirServlet`
  (devuelven JSON) y utilidades `Servicios`, `Json`, `Formato`.
- `modelo/`: `SoapClient` y `RespuestaServicio`.
- `webapp/`: `index.html` (login), `conversor.html`, `css/`, `js/`, `Imagenes/`.

## Pruebas
`mvn test` (pruebas del modelo y del JSON; no necesita el servidor real).
