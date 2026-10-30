# API_ConUni_GR07 - Servidor REST (.NET)

Servicio web **REST** (ASP.NET Core Web API, .NET 8) para convertir unidades de **longitud, masa y
temperatura**. Sin base de datos: credenciales, catálogo y tokens viven en memoria.

Es el equivalente RESTful de los servidores SOAP `WS_ConUni_Java_GR07` (TI.1.1) y
`WS_ConUni_DotNet_GR07` (TI.1.2): **mismas reglas de negocio, mismos códigos de error**;
cambia el transporte (JSON sobre HTTP en lugar de XML sobre SOAP).

## Requisitos
- .NET SDK 8 o superior (se desarrolló con el SDK 10)
- Visual Studio 2022/2026 (opcional) o la línea de comandos `dotnet`

## Compilar y probar
```bash
dotnet test        # compila y ejecuta las pruebas (incluye una prueba REST real, de extremo a extremo)
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
- `Seguridad`: `GestorTokens` (tokens en memoria) y `ServicioAutenticacion` (login).
- `Servicios`: `CatalogoUnidades`, la interfaz `IConversor` y los conversores de cada categoría.
- `Modelos/Utilidades`: `RespuestaServicio`, la respuesta estructurada común (JSON).
- `Modelos/Utilidades/Enums`: `CodigoResultado` (OK-200, AUTH-001, ...) y `EstadoRespuesta`.
- `Modelos/Utilidades/Mapeadores`: `RespuestaMapeador`, el único lugar donde se arma la respuesta.
- `Modelos/Peticiones`: `LoginRequest`, `ConversionRequest` — los cuerpos JSON de entrada,
  exclusivos de la variante REST.
- `API_ConUni_GR07.Pruebas`: pruebas xUnit (lógica, tokens y REST de extremo a extremo).

## Agregar divisas más adelante (pendiente de confirmar con el tutor)
1. Crear `ConversorDivisas : IConversor` en `Servicios` (categoría "Divisas", sus unidades y el cálculo).
2. Registrarlo en `CatalogoUnidades` (una línea).
3. Agregar `[HttpPost("divisas")]` a `ConversionesController` delegando en el mismo
   `ManejadorConversion.Convertir("Divisas", ...)`.
El catálogo, el token, la validación y los códigos de error funcionan igual.
