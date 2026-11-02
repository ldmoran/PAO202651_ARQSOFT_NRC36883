# REST_ConUni_Java_GR07

API REST JSON del Conversor de Unidades Monster. Implementada con Jakarta REST (JAX-RS) sobre Jakarta EE 10. Usa una base de datos local MySQL 8 (`conversion_unidades`): usuarios, catálogo de unidades, sesiones (tokens) e historial de conversiones.

## Requisitos

- JDK 21
- Maven 3.9+
- Payara 6 o GlassFish 7
- MySQL 8 con la base creada (ver `../../03. BDD/README.txt`)

## Base de datos

1. Ejecutar como `root`, en este orden, los scripts de `03. BDD`: `01_CrearUsuarioApp.sql`, `02_CreaBD_ConversionUnidades.sql` y `03_CargaDatos_ConversionUnidades.sql`.
2. Entregar la clave del usuario `conuni_app` al servidor con la propiedad `-Dconuni.db.clave=...` o con la variable de entorno `CONUNI_DB_CLAVE` (en Payara: `asadmin create-jvm-options "-Dconuni.db.clave=LA_CLAVE"` y reiniciar el dominio). La conexión por defecto está en `src/main/resources/conuni.properties`.
3. Las pruebas (`mvn test`) no necesitan MySQL: usan H2 en memoria, creada con los mismos scripts de `03. BDD`.

## Compilar y probar

Desde esta carpeta:

```bash
mvn clean package
```

Maven ejecuta las pruebas unitarias y genera `target/REST_ConUni_Java_GR07.war`.

## Desplegar

Despliega el WAR generado en Payara 6 (igual que en el proyecto 1.1) o GlassFish 7. El servidor es una aplicación web empaquetada como WAR que publica recursos REST por HTTP/JSON; no es una página web de interfaz ni un servicio SOAP. La URL base local es:

```text
http://localhost:8080/REST_ConUni_Java_GR07/api
```

## Recursos

Las operaciones protegidas reciben el token en la cabecera `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/login` | Inicia sesión |
| GET | `/catalogo/categorias` | Lista las categorías |
| GET | `/catalogo/{categoria}/unidades` | Lista las unidades de una categoría |
| POST | `/conversiones/{categoria}` | Convierte unidades de una categoría |

### Iniciar sesión

```http
POST /api/auth/login
Content-Type: application/json

{"usuario":"monster","clave":"monster9"}
```

Respuesta exitosa (`200 OK`):

```json
{
  "estado": "OK",
  "codigo": "OK-200",
  "mensaje": "Autenticación exitosa",
  "resultado": null,
  "unidadOrigen": null,
  "unidadDestino": null,
  "token": "<token>",
  "datos": []
}
```

### Consultar catálogo

```http
GET /api/catalogo/categorias
Authorization: Bearer <token>
```

```http
GET /api/catalogo/Longitud/unidades
Authorization: Bearer <token>
```

### Convertir unidades

```http
POST /api/conversiones/Longitud
Authorization: Bearer <token>
Content-Type: application/json

{"valor":10,"unidadOrigen":"metros","unidadDestino":"pies"}
```

El formato común de respuesta usa los campos `estado`, `codigo`, `mensaje`, `resultado`, `unidadOrigen`, `unidadDestino`, `token` y `datos`. Los nombres de unidades y categorías no distinguen mayúsculas ni tildes.

## Estados HTTP y códigos

| HTTP | Código(s) | Significado |
|---|---|---|
| 200 | `OK-200` | Operación correcta |
| 400 | `DATA-001` | Datos vacíos o inconsistentes (incluidas unidades de categorías distintas) |
| 401 | `AUTH-001`, `AUTH-002`, `AUTH-003` | Token inválido/expirado o credenciales incorrectas |
| 422 | `UNIT-001` | Unidad no soportada |
| 500 | `SRV-500` | Error interno |

La sesión se guarda en la tabla `sesion` y vence a los 30 minutos. Se puede ajustar con la propiedad de sistema `conuni.token.minutos`. Las credenciales de práctica son `monster` / `monster9`.

## Estructura

- `controladores.rest`: recursos Jakarta REST (JAX-RS), DTO JSON y traducción de respuestas a HTTP.
- `controladores.manejadoresws`: orquestación de autenticación, catálogo y conversiones.
- `datos`: acceso a MySQL por JDBC (`FuenteDatos`, `ConexionMySql` y un repositorio por tabla).
- `modelos.entidades`: `Usuario`, `Categoria` y `Unidad`.
- `seguridad`: autenticación (clave con sal y SHA-256) y tokens guardados en la tabla `sesion`.
- `servicios`: catálogo de unidades leído de la base y `Conversor`, que convierte con `factor_base` y `desplazamiento`.
- `modelos.utilidades`: respuesta JSON estructurada común.
