# ClienteConsola - Conversión de Unidades Monster (GR07, .NET, REST)

Cliente de consola (C#, .NET 10) de la API REST `API_ConUni_GR07`. Menú de opciones numéricas:
iniciar sesión, ver el catálogo, convertir y cerrar sesión. No contiene lógica de conversión.

## Ejecutar
Con el servidor REST en marcha (`http://localhost:5080/`):
```bash
dotnet run
```
Otra dirección del servidor: argumento (`dotnet run -- http://otro-equipo:5080/`) o variable de entorno `CONUNI_URL`.

Credenciales: `monster` / `monster9`.

## Estructura (MVC)
- `Modelos/`: `RestClient` (arma el cuerpo JSON y lo envía con el token Bearer), `RespuestaServicio`, `Sesion` (token).
- `Controladores/`: `ControladorSesion` y `ControladorConversiones`.
- `Vistas/`: `InterfazConsola` (menú), `VistaSesion`, `VistaConversion` y `ConsolaIO` (entrada/salida).
