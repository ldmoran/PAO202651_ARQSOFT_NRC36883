# WS_ConUni_DotNet_GR07 - Servidor SOAP (.NET)

Servicio web SOAP (**CoreWCF** sobre ASP.NET Core, .NET 8) para convertir unidades de **longitud, masa y temperatura**.
Sin base de datos: credenciales, catálogo y tokens viven en memoria.

Es el equivalente en .NET del servidor Java `WS_ConUni_Java_GR07`: **mismo contrato** (servicios, operaciones,
espacio de nombres `http://WebServices.monster.edu.ec/`, respuesta estructurada y códigos de error).

## Requisitos
- .NET SDK 8 o superior (se desarrolló con el SDK 10)
- Visual Studio 2022/2026 (opcional) o la línea de comandos `dotnet`

## Compilar y probar
```bash
dotnet test        # compila y ejecuta las 21 pruebas (incluye una prueba SOAP real, de extremo a extremo)
```

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
1. `WSLogin.login(usuario, clave)` devuelve un token. Credenciales: `monster` / `monster9`.
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

Unidades (en minúsculas y sin tildes; el servidor también acepta mayúsculas y tildes):
- Longitud: centimetros, metros, pies, yardas, millas
- Masa: gramos, kilogramos, onzas, libras, quintales
- Temperatura: celsius, fahrenheit, kelvin, newton, reaumur

## Estructura (espacios de nombres `ec.edu.monster`)
- `Controladores/WS`: los servicios SOAP (`WSLogin`, `WSCatalogo`, `WSLongitud`, `WSMasa`, `WSTemperatura`),
  cada uno con su interfaz `[ServiceContract]`. Solo reciben el pedido y se lo pasan a un manejador.
- `Controladores/ManejadoresWS`: un manejador por servicio (`ManejadorLogin`, `ManejadorCatalogo`,
  `ManejadorConversion`). Orquestan el token, las validaciones y el cálculo.
- `Seguridad`: `GestorTokens` (tokens en memoria) y `ServicioAutenticacion` (login).
- `Servicios`: `CatalogoUnidades`, la interfaz `IConversor` y los conversores de cada categoría.
- `Modelos/Utilidades`: `RespuestaServicio`, la respuesta estructurada común.
- `Modelos/Utilidades/Enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `Modelos/Utilidades/Mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `WS_ConUni_DotNet_GR07.Pruebas`: pruebas xUnit (lógica, tokens y SOAP de extremo a extremo).

## Agregar divisas más adelante (pendiente de confirmar con el tutor)
1. Crear `ConversorDivisas : IConversor` en `Servicios` (categoría "Divisas", sus unidades y el cálculo).
2. Registrarlo en `CatalogoUnidades` (una línea).
3. Crear el servicio `WSDivisas` en `Controladores/WS` copiando `WSMasa`, y registrarlo en `Program.cs`.
El catálogo, el token, la validación y los códigos de error funcionan igual.
