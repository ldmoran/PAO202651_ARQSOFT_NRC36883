# Cliente de escritorio - Conversión de Unidades Monster (GR07, .NET, REST)

Contiene dos proyectos:

- **`SistemaConversorUI.Shared`**: biblioteca Razor con la interfaz (login y conversor), el cliente REST
  (`RestClient`) y la sesión (`SesionCliente`). La usan los clientes de escritorio, web y móvil, de modo
  que los tres comparten el mismo diseño (RNF-006) y el mismo código. No contiene lógica de conversión:
  todo el cálculo ocurre en el servidor.
- **`ClienteEscritorio`**: aplicación de escritorio de Windows (WPF + BlazorWebView, .NET 10) que aloja esa interfaz.

## Ejecutar
Con la API REST en marcha (`http://localhost:5080/`):
```bash
cd ClienteEscritorio
dotnet run
```
Dirección de la API: campo "Servidor" de la pantalla de inicio de sesión, o variable de entorno `CONUNI_URL`.
Credenciales: `monster` / `monster9`.

Requiere el runtime de WebView2 (viene con Windows 11 y con Microsoft Edge).
