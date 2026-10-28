# REST_ConUni_Java_GR07

API REST JSON del Conversor de Unidades Monster. Implementada con Jakarta REST (JAX-RS) sobre Jakarta EE 10. No utiliza base de datos: las credenciales, el catálogo y los tokens viven en memoria.

## Requisitos

- JDK 21
- Maven 3.9+
- Payara 6 o GlassFish 7

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

El token vence a los 30 minutos. Se puede ajustar con la propiedad de sistema `conuni.token.minutos`. Las credenciales de práctica son `monster` / `monster9`.

## Estructura

- `controladores.rest`: recursos Jakarta REST (JAX-RS), DTO JSON y traducción de respuestas a HTTP.
- `controladores.manejadoresws`: orquestación de autenticación, catálogo y conversiones.
- `seguridad`: autenticación y tokens en memoria.
- `servicios`: catálogo de unidades y conversores de longitud, masa y temperatura.
- `modelos.utilidades`: respuesta JSON estructurada común.
