# API_ConUni_GR07 - Servidor REST (.NET)

Servicio web **REST** (ASP.NET Core Web API, .NET 8) para convertir unidades de **longitud, masa y
temperatura**. Usa una base de datos local SQL Server (`conversion_unidades`): usuarios, catálogo de unidades, sesiones (tokens) e historial de conversiones.

Es el equivalente RESTful de los servidores SOAP `WS_ConUni_Java_GR07` (TI.1.1) y
`WS_ConUni_DotNet_GR07` (TI.1.2): **mismas reglas de negocio, mismos códigos de error**;
cambia el transporte (JSON sobre HTTP en lugar de XML sobre SOAP).

## Requisitos
- .NET SDK 8 o superior (se desarrolló con el SDK 10)
- Visual Studio 2022/2026 (opcional) o la línea de comandos `dotnet`
- SQL Server (Express o Developer) como instancia por defecto `localhost`, con la base creada (ver `../../03. BDD/README.txt`)

## Base de datos
1. Ejecutar en orden los scripts de `03. BDD` (`01_CrearBaseYUsuario.sql`, `02_CreaBD_ConversionUnidades.sql`, `03_CargaDatos_ConversionUnidades.sql`), por ejemplo con `sqlcmd -S localhost -E -i "<script>"`.
2. La cadena de conexión está en `API_ConUni_GR07/appsettings.json` (`ConnectionStrings:ConversionUnidades`, autenticación de Windows). Se puede reemplazar con la variable de entorno `ConnectionStrings__ConversionUnidades`.
3. `dotnet test` crea una base temporal con los mismos scripts y la borra al terminar; si no hay SQL Server en `localhost`, esas pruebas se omiten.

## Compilar y probar
```bash
dotnet test        # compila y ejecuta las pruebas (incluye una prueba REST real, de extremo a extremo, contra SQL Server local)
```

## Ejecutar
```bash
cd API_ConUni_GR07
dotnet run --no-launch-profile      # http://localhost:5080
```
Otra dirección: `dotnet run --no-launch-profile --urls http://localhost:7000`.
Documentación interactiva (equivalente al WSDL de las versiones SOAP): `http://localhost:5080/swagger`.

## Endpoints
| Método | Ruta | Requiere `Authorization: Bearer` |
|---|---|---|
| POST | `/api/auth/login` | No |
| POST | `/api/auth/logout` | Sí |
| GET | `/api/catalogo` | Sí |
| GET | `/api/catalogo/{categoria}` | Sí |
| POST | `/api/conversiones/longitud` | Sí |
| POST | `/api/conversiones/masa` | Sí |
| POST | `/api/conversiones/temperatura` | Sí |

## Uso
1. `POST /api/auth/login` con `{ "usuario": "monster", "clave": "monster9" }` devuelve un token.
2. Con ese token en la cabecera `Authorization: Bearer <token>`:
   `GET /api/catalogo`, `GET /api/catalogo/{categoria}` y
   `POST /api/conversiones/{longitud|masa|temperatura}` con
   `{ "valor": n, "unidadOrigen": "...", "unidadDestino": "..." }`.

El token vale 30 minutos. Para cambiarlo: variable de entorno `CONUNI_TOKEN_MINUTOS` (acepta decimales).

## Respuesta (todas las operaciones, HTTP 200 incluso en error — ver el ERS para la justificación)
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

## Estructura (espacio de nombres `ec.edu.monster`)
- `Controladores/`: los controladores REST (`AuthController`, `CatalogoController`,
  `ConversionesController`) y `ApiControllerBase` (extrae el token Bearer). Solo reciben el
  pedido HTTP y se lo pasan a un manejador.
- `Controladores/Manejadores/`: un manejador por controlador (`ManejadorLogin`,
  `ManejadorCatalogo`, `ManejadorConversion`). Orquestan el token, las validaciones y el cálculo.
- `Datos`: acceso a SQL Server con ADO.NET (`BaseDatos`, `ConexionSqlServer` y un repositorio por tabla).
- `Modelos/Entidades`: `Usuario`, `Categoria` y `Unidad`.
- `Seguridad`: `GestorTokens` (sesiones guardadas en la tabla `sesion`), `ServicioAutenticacion` (login) y `Claves` (sal y SHA-256).
- `Servicios`: `CatalogoUnidades` (leído de la base) y `Conversor`, que convierte con `factor_base` y `desplazamiento`.
- `Modelos/Utilidades`: `RespuestaServicio`, la respuesta estructurada común (JSON).
- `Modelos/Utilidades/Enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `Modelos/Utilidades/Mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `Modelos/Peticiones`: `LoginRequest`, `ConversionRequest` — los cuerpos JSON de entrada,
  exclusivos de la variante REST.
- `API_ConUni_GR07.Pruebas`: pruebas xUnit (lógica, tokens y REST de extremo a extremo).

## Agregar divisas más adelante (pendiente de confirmar con el tutor)
1. Insertar en la base la categoría "Divisas" y sus unidades (tablas `categoria` y `unidad`, con su `factor_base`); no hace falta programar un conversor.
2. Agregar `[HttpPost("divisas")]` a `ConversionesController` delegando en el mismo
   `ManejadorConversion.Convertir("Divisas", ...)`.
El catálogo, el token, la validación y los códigos de error funcionan igual.
