# Especificación de requisitos (ERS) - TI.1.3

## Propósito

Conversión de Unidades Monster permite que clientes de consola, escritorio, web y móvil se autentiquen y conviertan unidades de longitud, masa y temperatura mediante una API REST JSON escrita en Java. No se persisten usuarios, tokens ni conversiones.

## Actores y límites

- **Usuario:** inicia sesión, consulta el catálogo y solicita conversiones.
- **Clientes:** aplicaciones Java que consumen HTTP/JSON; no calculan conversiones.
- **API REST:** valida credenciales y tokens, valida solicitudes, consulta el catálogo en memoria y ejecuta el cálculo.

## Requisitos funcionales

| ID | Requisito |
|---|---|
| RF-01 | Autenticar las credenciales de práctica `monster` / `monster9` y devolver un token de sesión. |
| RF-02 | Consultar las categorías de conversión disponibles. |
| RF-03 | Consultar las unidades disponibles para una categoría. |
| RF-04 | Convertir un valor entre unidades de una misma categoría. |
| RF-05 | Rechazar solicitudes protegidas con token ausente, inválido o expirado. |
| RF-06 | Responder errores con código y mensaje estructurados en JSON. |

## Requisitos no funcionales

- Jakarta REST (JAX-RS) sobre Jakarta EE 10, Java 21.
- Recursos HTTP sin estado; el token se envía mediante `Authorization: Bearer`.
- Catálogo, credenciales y sesiones permanecen en memoria; no hay acceso a BDD.
- Las respuestas comparten la forma `estado`, `codigo`, `mensaje`, `resultado`, `unidadOrigen`, `unidadDestino`, `token` y `datos`.

## Reglas del dominio

- Categorías: Longitud, Masa y Temperatura.
- Unidades de longitud: centimetros, metros, pies, yardas y millas.
- Unidades de masa: gramos, kilogramos, onzas, libras y quintales.
- Unidades de temperatura: celsius, fahrenheit, kelvin, newton y reaumur.
- Los nombres de categoría y unidad ignoran mayúsculas y tildes.
- Los tokens vencen por defecto a los 30 minutos; la propiedad `conuni.token.minutos` permite ajustar la duración.

## Criterios de aceptación

1. Login válido devuelve HTTP 200 y un token no vacío; credenciales inválidas devuelven HTTP 401.
2. Sin token válido, catálogo y conversión devuelven HTTP 401.
3. Las consultas de catálogo devuelven datos coherentes con las tres categorías.
4. Una conversión válida devuelve resultado, unidades normalizadas y código `OK-200`.
5. Solicitudes inválidas reciben códigos estables y un estado HTTP acorde al tipo de error.
6. El sistema funciona sin crear tablas ni configurar una base de datos.
