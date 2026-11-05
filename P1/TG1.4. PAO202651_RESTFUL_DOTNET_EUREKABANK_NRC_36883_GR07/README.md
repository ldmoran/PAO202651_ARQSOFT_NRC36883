# TG1.4 – EurekaBank · RESTful en .NET con base de datos local (SQL Server)

Arquitectura de Software · NRC 36883 · Grupo 7 (Miranda, Morán, Vivanco)

Servicio REST de EurekaBank (.NET 8 + ASP.NET Core Web API) que guarda todo en SQL Server (`eurekabank`), con
cuatro clientes: consola, escritorio, web y móvil. Es la misma aplicación que TG1.2 (mismas reglas de negocio,
misma base de datos, mismo diseño de interfaz): lo único que cambia es el transporte, de SOAP/WSDL a HTTP/JSON.

## Estructura
| Carpeta | Contenido |
|---|---|
| `04. SERVIDOR/API_EurekaBank_RESTDOTNET_GR07` | Servidor REST (puerto 5081, documentación interactiva en `/swagger`) |
| `04. CLICON/ClienteConsola` | Cliente de consola (.NET 10) |
| `04. CLIESC` | `EurekaBankUI.Shared` (interfaz Blazor compartida) y `ClienteEscritorio` (WPF + BlazorWebView) |
| `04. CLIWEB/ClienteWeb` | Cliente web (Blazor Server, puerto 5090) |
| `04. CLIMOV/ClienteMovil` | Cliente móvil Android (.NET MAUI Blazor) + `ClienteMovil-GR07.apk` ya compilado |
| `03. BDD` | Scripts de SQL Server: tablas y datos iniciales (idénticos a TG1.2, el modelo de datos no cambia) |
| `01. UML` | `01. ERS` (requisitos), `02. ECUD` (casos de uso) y `03. DIAGRAMAS` (casos de uso, actividad, secuencia, clases y arquitectura: Power Designer + PNG) |
| `02. MER` | Modelo entidad-relación: conceptual, lógico y físico (Power Designer + PNG) |
| `05. DOCUMENTACION` | Diccionario de datos, manual técnico, manual de usuario, evidencias (capturas) y presentación |

## De SOAP a REST: qué cambió y qué no
- **No cambia**: reglas de negocio (`ServicioEureka`), acceso a datos (`EurekaRepositorio`, transacciones de
  depósito/retiro/transferencia), el modelo de base de datos y toda la interfaz de usuario (los mismos
  `Login.razor` y `Panel.razor` de `EurekaBankUI.Shared`, el mismo CSS y las mismas imágenes).
- **Cambia**: la capa de transporte. El contrato SOAP (`IWSEurekaBank` sobre `CoreWCF`/`BasicHttpBinding`) se
  reemplaza por controladores REST (`Microsoft.AspNetCore.Mvc`) sobre JSON:

| Operación SOAP (TG1.2) | Endpoint REST (TG1.4) |
|---|---|
| `iniciarSesion(usuario, contrasena)` | `POST /api/sesiones` |
| `traerCuentasConClientes()` | `GET /api/cuentas` |
| `traerMovimientos(cuenta)` | `GET /api/movimientos?cuenta=...` |
| `tipoMovimientosPermitidos()` | `GET /api/movimientos/tipos` |
| `regMovimiento(origen, destino, tipo, importe)` | `POST /api/movimientos` |

En el lado del cliente, `SoapClient` (armaba y parseaba sobres SOAP/XML) se reemplaza por `RestClient`
(`HttpClient` + `System.Text.Json`), con exactamente la misma superficie pública (`IniciarSesion`, `Cuentas`,
`Movimientos`, `Tipos`, `Registrar`), así que `SesionCliente` y toda la interfaz Blazor no necesitaron cambios.

## Base de datos
Tablas: `moneda`, `sucursal`, `empleado`, `usuario` (clave con sal + SHA-256), `cliente`, `cuenta`, `tipo_movimiento`,
`movimiento` e `intento_acceso`. Las transferencias, depósitos y retiros corren en una transacción (bloqueo de filas).
```bash
sqlcmd -S localhost -E -i "03. BDD/01_CrearBaseYTablas.sql"
sqlcmd -S localhost -E -i "03. BDD/02_CargaDatos.sql"
```
Usuarios de demostración: `lmoran`, `amiranda`, `gvivanco` · clave `Eureka2026!`.
Cuentas de ejemplo: `00100001`, `00100002`, `00200001`, `00200002`, `00100003`.

## Ejecutar
```bash
cd "04. SERVIDOR/API_EurekaBank_RESTDOTNET_GR07"
dotnet run                       # http://localhost:5081  (Swagger en /swagger)
cd "04. CLICON/ClienteConsola" && dotnet run
cd "04. CLIWEB/ClienteWeb" && dotnet run --no-launch-profile      # http://localhost:5090
cd "04. CLIESC/ClienteEscritorio" && dotnet run
```
Para usar otro servidor: variable `EUREKA_URL` (por ejemplo `http://IP:5081`) o el campo "Servidor" del login.
Para que otros equipos se conecten: `dotnet run --urls http://0.0.0.0:5081` y abrir el puerto en el firewall.

## Móvil
APK listo en `04. CLIMOV/ClienteMovil-GR07.apk`. En el login, abrir "Servidor" y escribir `http://IP_DEL_PC:5081`
(en el emulador es `10.0.2.2`).
Compilar: `dotnet build -f net10.0-android -c Release` (copiar el proyecto a una ruta corta, por ejemplo
`C:\eub`, por el límite de 260 caracteres de Windows).

## Archivos en la raíz y pruebas
- `EurekaBank_RESTDOTNET_GR07.sln`: solución de Visual Studio con el servidor, sus pruebas, la consola, la interfaz compartida, el escritorio y la web (el móvil MAUI se abre aparte).
- `POWER DESIGNER/`: modelos de Power Designer (`MER` y `UML`, incluidos clases y actividad).
- Pruebas: `04. SERVIDOR/API_EurekaBank_RESTDOTNET_GR07.Pruebas` (35 pruebas xUnit: servicio + API REST por HTTP). Ejecutar con `dotnet test`.
- Documentación completa en `05. DOCUMENTACION` (ERS, ECUD, diccionario, manuales, presentación y capturas en `05. DOCUMENTACION/04. EVIDENCIAS`).
