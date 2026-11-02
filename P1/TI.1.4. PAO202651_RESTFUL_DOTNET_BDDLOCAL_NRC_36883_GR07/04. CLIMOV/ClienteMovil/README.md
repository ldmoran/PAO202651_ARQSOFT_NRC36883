# ClienteMovil - Conversión de Unidades Monster (GR07, .NET MAUI Android, REST)

Cliente móvil Android (.NET MAUI Blazor Hybrid, .NET 10) de la API REST `API_ConUni_GR07`.
Usa la interfaz compartida `04. CLIESC/SistemaConversorUI.Shared` (mismo diseño que el escritorio y la web).

## Requisitos
- .NET SDK 10 con la carga de trabajo `maui-android` (`dotnet workload install maui-android`)
- Android SDK (con un emulador o un teléfono con depuración USB)

## Ejecutar
Con la API REST en marcha en el equipo (`http://localhost:5080/`):
```bash
dotnet build -t:Run -f net10.0-android
```
- **Emulador:** el equipo se ve como `10.0.2.2`; esa es la dirección por defecto (`MauiProgram.UrlServidorEmulador`).
- **Teléfono real:** en la pantalla de inicio de sesión abrir "Servidor" y escribir `http://IP-DEL-EQUIPO:5080/`
  (la API debe escucharse en la red: `dotnet run --no-launch-profile --urls http://0.0.0.0:5080`).

Credenciales: `monster` / `monster9`.
