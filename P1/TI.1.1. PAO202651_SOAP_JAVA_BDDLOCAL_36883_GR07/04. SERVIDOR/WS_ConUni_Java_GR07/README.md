# WS_ConUni_Java_GR07 - Servidor SOAP

Servicio web SOAP (Jakarta JAX-WS) para convertir unidades de **longitud, masa y temperatura**.
Con base de datos local **MySQL** (`conversion_unidades`): usuarios, catálogo de unidades, sesiones (tokens),
bitácora de accesos e historial de conversiones.

## Requisitos
- JDK 21
- Maven 3.9+
- Servidor de aplicaciones Jakarta EE 10 (Payara 6 o GlassFish 7)
- MySQL 8 (servicio `MySQL80`)

## Compilar y probar
```bash
mvn clean package        # compila, ejecuta las 28 pruebas y genera target/WS_ConUni_Java_GR07.war
```
Las pruebas incluyen una prueba SOAP real (se publican los servicios y se invocan por HTTP). Usan una base H2 en memoria que
se crea con los mismos scripts de `03. BDD`, así que no necesitan MySQL encendido.

## Base de datos
Scripts en `03. BDD` (en este orden, la primera vez):
```bash
mysql -u root -p < 01_CrearUsuarioApp.sql           # base conversion_unidades y usuario conuni_app
mysql -u root -p < 02_CreaBD_ConversionUnidades.sql # tablas, índices y vista
mysql -u root -p < 03_CargaDatos_ConversionUnidades.sql  # usuario monster y catálogo
```
La conexión se lee de `-Dconuni.db.url/usuario/clave`, de las variables `CONUNI_DB_URL/USUARIO/CLAVE` o de
`conuni.properties` (sin clave). En Payara: `asadmin create-jvm-options "-Dconuni.db.clave=ConUni2026!"`.

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
1. `WSLogin.login(usuario, clave)` devuelve un token. Credenciales: `monster` / `monster9` (en la base solo se guarda su hash con sal).
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

Unidades (están en la tabla `unidad`; en minúsculas y sin tildes, y el servidor también acepta mayúsculas y tildes):
- Longitud: centimetros, metros, pies, yardas, millas
- Masa: gramos, kilogramos, onzas, libras, quintales
- Temperatura: celsius, fahrenheit, kelvin, newton, reaumur

## Estructura (paquetes `ec.edu.monster`)
- `controladores.ws`: los servicios SOAP (`WSLogin`, `WSCatalogo`, `WSLongitud`, `WSMasa`,
  `WSTemperatura`). Solo reciben el pedido y se lo pasan a un manejador.
- `controladores.manejadoresws`: un manejador por servicio (`ManejadorLogin`, `ManejadorCatalogo`,
  `ManejadorConversion`). Orquestan el token, las validaciones y el cálculo.
- `datos`: acceso a MySQL (`ConexionMySql`, `Datos` y un repositorio por tabla).
- `seguridad`: `Claves` (sal + SHA-256), `GestorTokens` (sesiones en la base) y `ServicioAutenticacion` (login y bitácora).
- `servicios`: `CatalogoUnidades` (lee categorías y unidades de la base) y `Conversor` (factor y desplazamiento).
- `modelos.entidades`: `Usuario`, `Categoria` y `Unidad`.
- `modelos.utilidades`: `RespuestaServicio`, la respuesta estructurada común.
- `modelos.utilidades.enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `modelos.utilidades.mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `src/test/.../pruebas`: `ServidorLocal` (lanzador para probar sin Payara).

El espacio de nombres SOAP está fijado en cada servicio (`http://WebServices.monster.edu.ec/`), así
que el contrato (WSDL) y los cuatro clientes no dependen del nombre de los paquetes.
