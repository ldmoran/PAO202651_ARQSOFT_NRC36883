# TG1.2 – EurekaBank · SOAP en .NET con base de datos local (SQL Server)

Arquitectura de Software · NRC 36883 · Grupo 7 (Miranda, Morán, Vivanco)

Servicio SOAP de EurekaBank (.NET 8 + CoreWCF) que guarda todo en SQL Server (`eurekabank`), con cuatro clientes:
consola, escritorio, web y móvil. El contrato SOAP (espacio de nombres y operaciones) es el mismo del servidor Java
(TG1.1), así que **los mismos clientes sirven con los dos servidores cambiando solo la URL**.

## Estructura
| Carpeta | Contenido |
|---|---|
| `04. SERVIDOR/WS_EurekaBank_SOAPDOTNET_GR07` | Servidor SOAP (puerto 5081, ruta `/WSEurekaBank`) |
| `04. CLICON/ClienteConsola` | Cliente de consola (.NET 10) |
| `04. CLIESC` | `EurekaBankUI.Shared` (interfaz Blazor compartida) y `ClienteEscritorio` (WPF + BlazorWebView) |
| `04. CLIWEB/ClienteWeb` | Cliente web (Blazor Server, puerto 5090) |
| `04. CLIMOV/ClienteMovil` | Cliente móvil Android (.NET MAUI Blazor) |
| `03. BDD` | Scripts de SQL Server: tablas y datos iniciales |
| `01. UML` | `01. ERS` (requisitos), `02. ECUD` (casos de uso) y `03. DIAGRAMAS` (casos de uso, actividad, secuencia, clases y arquitectura: Power Designer + PNG) |
| `02. MER` | Modelo entidad-relación: conceptual, lógico y físico (Power Designer + PNG) |
| `05. DOCUMENTACION` | Diccionario de datos, manual técnico, manual de usuario, evidencias (capturas) y presentación |

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
cd "04. SERVIDOR/WS_EurekaBank_SOAPDOTNET_GR07/WS_EurekaBank_SOAPDOTNET_GR07"
dotnet run                       # http://localhost:5081/WSEurekaBank?wsdl
cd "04. CLICON/ClienteConsola" && dotnet run
cd "04. CLIWEB/ClienteWeb" && dotnet run --no-launch-profile      # http://localhost:5090
cd "04. CLIESC/ClienteEscritorio" && dotnet run
```
Para usar otro servidor: variable `EUREKA_URL` (por ejemplo `http://IP:5081/WSEurekaBank`) o el campo "Servidor" del login.
Para que otros equipos se conecten: `dotnet run --urls http://0.0.0.0:5081` y abrir el puerto en el firewall.

## Móvil
APK listo en `documento para leer/guia/EurekaBank_SOAP_DotNet_GR07.apk`. En el login, abrir "Servidor" y escribir
`http://IP_DEL_PC:5081/WSEurekaBank` (en el emulador es `10.0.2.2`).
Compilar: `dotnet build -f net10.0-android -c Debug -p:EmbedAssembliesIntoApk=true` (copiar el proyecto a una ruta corta,
por ejemplo `C:\eub`, por el límite de 260 caracteres de Windows).

## Archivos en la raíz
- `EurekaBank_SOAPDOTNET_GR07.sln`: solución de Visual Studio con el servidor, sus pruebas, la consola, la interfaz compartida, el escritorio y la web (el móvil MAUI se abre aparte: `04. CLIMOV/ClienteMovil`).
- `POWER DESIGNER/`: modelos de Power Designer (`MER/*.cdm, *.ldm, *.pdm` y `UML/*.oom`).
