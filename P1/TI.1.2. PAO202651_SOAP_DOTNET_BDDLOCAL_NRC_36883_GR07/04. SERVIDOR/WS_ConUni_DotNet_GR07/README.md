# WS_ConUni_DotNet_GR07 - Servidor SOAP (.NET)

Servicio web SOAP (**CoreWCF** sobre ASP.NET Core, .NET 8) para convertir unidades de **longitud, masa y temperatura**.
Con base de datos local **SQL Server** (`conversion_unidades`): usuarios, catálogo de unidades, sesiones (tokens),
bitácora de accesos e historial de conversiones.

Es el equivalente en .NET del servidor Java `WS_ConUni_Java_GR07`: **mismo contrato** (servicios, operaciones,
espacio de nombres `http://WebServices.monster.edu.ec/`, respuesta estructurada y códigos de error).

## Requisitos
- .NET SDK 8 o superior (se desarrolló con el SDK 10)
- Visual Studio 2022/2026 (opcional) o la línea de comandos `dotnet`
- SQL Server 2019 o superior (sirve la edición Express) y `sqlcmd`

## Compilar y probar
```bash
dotnet test        # compila y ejecuta las 28 pruebas (incluye una prueba SOAP real, de extremo a extremo)
```
Las pruebas crean una base temporal en `localhost` con los mismos scripts de `03. BDD` (autenticación de Windows) y la borran al terminar;
si no hay SQL Server se omiten. Importante: `Directory.Build.props` compila para `win-x64` para que la DLL nativa de SQL Server no
quede en una ruta de más de 260 caracteres; si copias el proyecto, hazlo a una carpeta corta (por ejemplo `C:\Entrega`).

## Base de datos
Scripts en `03. BDD` (en este orden, la primera vez):
```bash
sqlcmd -S localhost -E -C -i 01_CrearBaseYUsuario.sql
sqlcmd -S localhost -E -C -b -i 02_CreaBD_ConversionUnidades.sql
sqlcmd -S localhost -E -C -b -i 03_CargaDatos_ConversionUnidades.sql
```
La cadena de conexión está en `appsettings.json` (`ConnectionStrings:ConversionUnidades`, autenticación de Windows) y se puede
reemplazar con la variable de entorno `CONUNI_DB_CONEXION` (por ejemplo, para usar el inicio de sesión `conuni_app`).

## Ejecutar
```bash
cd WS_ConUni_DotNet_GR07
dotnet run --no-launch-profile      # http://localhost:5080
```
Otra dirección: `dotnet run --no-launch-profile --urls http://localhost:7000`.

| Servicio | URL del WSDL |
|---|---|
| WSLogin | `/WSLogin?wsdl` |
| WSCatalogo | `/WSCatalogo?wsdl` |
| WSLongitud | `/WSLongitud?wsdl` |
| WSMasa | `/WSMasa?wsdl` |
| WSTemperatura | `/WSTemperatura?wsdl` |

## Uso
1. `WSLogin.login(usuario, clave)` devuelve un token. Credenciales: `monster` / `monster9` (en la base solo se guarda su hash con sal).
2. Con ese token: `WSCatalogo.obtenerCategorias`, `WSCatalogo.obtenerUnidades` y las conversiones
   `convertirLongitud`, `convertirMasa`, `convertirTemperatura` (valor, unidadOrigen, unidadDestino, token).

El token vale 30 minutos. Para cambiarlo: variable de entorno `CONUNI_TOKEN_MINUTOS` (acepta decimales).

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

## Estructura (espacios de nombres `ec.edu.monster`)
- `Controladores/WS`: los servicios SOAP (`WSLogin`, `WSCatalogo`, `WSLongitud`, `WSMasa`, `WSTemperatura`),
  cada uno con su interfaz `[ServiceContract]`. Solo reciben el pedido y se lo pasan a un manejador.
- `Controladores/ManejadoresWS`: un manejador por servicio (`ManejadorLogin`, `ManejadorCatalogo`,
  `ManejadorConversion`). Orquestan el token, las validaciones y el cálculo.
- `Datos`: acceso a SQL Server (`ConexionSqlServer`, `BaseDatos` y un repositorio por tabla).
- `Seguridad`: `Claves` (sal + SHA-256), `GestorTokens` (sesiones en la base) y `ServicioAutenticacion` (login y bitácora).
- `Servicios`: `CatalogoUnidades` (lee categorías y unidades de la base) y `Conversor` (factor y desplazamiento).
- `Modelos/Entidades`: `Usuario`, `Categoria` y `Unidad`.
- `Modelos/Utilidades`: `RespuestaServicio`, la respuesta estructurada común.
- `Modelos/Utilidades/Enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `Modelos/Utilidades/Mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `WS_ConUni_DotNet_GR07.Pruebas`: pruebas xUnit (lógica, tokens y SOAP de extremo a extremo).
