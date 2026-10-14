# WS_ConUni_Java_GR07 - Servidor SOAP

Servicio web SOAP (Jakarta JAX-WS) para convertir unidades de **longitud, masa y temperatura**.
Sin base de datos: credenciales, catálogo y tokens viven en memoria.

## Requisitos
- JDK 21
- Maven 3.9+
- Servidor de aplicaciones Jakarta EE 10 (Payara 6 o GlassFish 7)

## Compilar y probar
```bash
mvn clean package        # compila, ejecuta las 21 pruebas y genera target/WS_ConUni_Java_GR07.war
```
Las pruebas incluyen una prueba SOAP real (se publican los servicios y se invocan por HTTP).

## Desplegar
Despliega `target/WS_ConUni_Java_GR07.war` en Payara (desde NetBeans: Run, o `asadmin deploy`).
Contexto: `http://localhost:8080/WS_ConUni_Java_GR07/`

| Servicio | URL del WSDL |
|---|---|
| WSLogin | `/WS_ConUni_Java_GR07/WSLogin?wsdl` |
| WSCatalogo | `/WS_ConUni_Java_GR07/WSCatalogo?wsdl` |
| WSLongitud | `/WS_ConUni_Java_GR07/WSLongitud?wsdl` |
| WSMasa | `/WS_ConUni_Java_GR07/WSMasa?wsdl` |
| WSTemperatura | `/WS_ConUni_Java_GR07/WSTemperatura?wsdl` |

## Uso
1. `WSLogin.login(usuario, clave)` devuelve un token. Credenciales: `monster` / `monster9`.
2. Con ese token: `WSCatalogo.obtenerCategorias`, `WSCatalogo.obtenerUnidades` y las conversiones
   `convertirLongitud`, `convertirMasa`, `convertirTemperatura` (valor, unidadOrigen, unidadDestino, token).

El token vale 30 minutos. Para cambiarlo: propiedad del sistema `conuni.token.minutos`.

## Respuesta (todas las operaciones)
`estado` (OK/ERROR), `codigo`, `mensaje`, `resultado`, `unidadOrigen`, `unidadDestino`,
más `token` (login) y `datos` (catálogo).

| Código | Significado |
|---|---|
| OK-200 | Operación correcta |
| AUTH-001 | Token inválido |
| AUTH-002 | Sesión expirada |
| AUTH-003 | Usuario o clave incorrectos |
| UNIT-001 | Unidad de origen o destino no soportada |
| DATA-001 | Datos inconsistentes (vacíos, valor no numérico, categorías distintas) |
| SRV-500 | Error interno |

Unidades (en minúsculas y sin tildes; el servidor también acepta mayúsculas y tildes):
- Longitud: centimetros, metros, pies, yardas, millas
- Masa: gramos, kilogramos, onzas, libras, quintales
- Temperatura: celsius, fahrenheit, kelvin, newton, reaumur

## Estructura (paquetes `ec.edu.monster`)
- `controladores.ws`: los servicios SOAP (`WSLogin`, `WSCatalogo`, `WSLongitud`, `WSMasa`,
  `WSTemperatura`). Solo reciben el pedido y se lo pasan a un manejador.
- `controladores.manejadoresws`: un manejador por servicio (`ManejadorLogin`, `ManejadorCatalogo`,
  `ManejadorConversion`). Orquestan el token, las validaciones y el cálculo.
- `seguridad`: `GestorTokens` (tokens en memoria) y `ServicioAutenticacion` (login).
- `servicios`: `CatalogoUnidades`, la interfaz `Conversor` y los conversores de cada categoría.
- `modelos.utilidades`: `RespuestaServicio`, la respuesta estructurada común.
- `modelos.utilidades.enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `modelos.utilidades.mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `src/test/.../pruebas`: `ServidorLocal` (lanzador para probar sin Payara).

El espacio de nombres SOAP está fijado en cada servicio (`http://WebServices.monster.edu.ec/`), así
que el contrato (WSDL) y los cuatro clientes no dependen del nombre de los paquetes.

## Agregar divisas más adelante (pendiente de confirmar con el tutor)
La lógica ya está separada para sumar una categoría sin tocar lo demás:
1. Crear `ConversorDivisas implements Conversor` en `servicios` (categoría "Divisas", sus unidades y el cálculo).
2. Registrarlo en `CatalogoUnidades` (una línea).
3. Crear el servicio `WSDivisas` en `controladores.ws` copiando `WSMasa` y cambiando la categoría.
El catálogo, el token, la validación y los códigos de error funcionan igual.
