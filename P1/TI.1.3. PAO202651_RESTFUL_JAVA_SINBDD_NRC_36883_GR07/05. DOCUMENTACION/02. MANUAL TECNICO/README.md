# Manual técnico

## Stack y despliegue

- Java 21, Maven 3.9+, Jakarta EE 10 y Jakarta REST.
- Empaquetado WAR desplegado en Payara 6 (igual que el proyecto 1.1) o GlassFish 7.
- Sin BDD: credenciales de práctica, catálogo y tokens residen en memoria.
- API base desplegada: `http://localhost:8080/REST_ConUni_Java_GR07/api`.

Desde `04. SERVIDOR/REST_ConUni_Java_GR07`, ejecutar `mvn clean package` y desplegar `target/REST_ConUni_Java_GR07.war`.

## Contrato HTTP

| Verbo | Endpoint | JSON de entrada |
|---|---|---|
| POST | `/auth/login` | `{"usuario":"monster","clave":"monster9"}` |
| GET | `/catalogo/categorias` | Ninguno |
| GET | `/catalogo/{categoria}/unidades` | Ninguno |
| POST | `/conversiones/{categoria}` | `{"valor":10,"unidadOrigen":"metros","unidadDestino":"pies"}` |

Los tres últimos requieren cabecera `Authorization: Bearer <token>`. Vigencia predeterminada del token: 30 minutos (`conuni.token.minutos`).

## Módulos

`controladores.rest` mapea HTTP/JSON; `controladores.manejadoresws` coordina operaciones; `seguridad` administra login y tokens; `servicios` contiene catálogo y conversores; `modelos.utilidades` implementa la respuesta uniforme.

Los proyectos cliente contienen pruebas unitarias contra un servidor HTTP falso. Los cuatro proyectos Maven pueden validarse con `mvn test`; la app Android incluye pruebas JUnit ejecutables con `gradlew test`.
