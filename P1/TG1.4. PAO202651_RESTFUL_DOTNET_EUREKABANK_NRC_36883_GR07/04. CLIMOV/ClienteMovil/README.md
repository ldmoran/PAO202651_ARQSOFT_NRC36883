# ClienteMovil - EurekaBank (GR07, .NET MAUI Android)

Cliente móvil Android (.NET MAUI Blazor Hybrid, .NET 10) del servicio REST `API_EurekaBank_RESTDOTNET_GR07`.
Usa la interfaz compartida `04. CLIESC/EurekaBankUI.Shared` (mismo diseño que el escritorio y la web).

## Requisitos
- .NET SDK 10 con la carga de trabajo `maui-android` (`dotnet workload install maui-android`)
- Android SDK (con un emulador o un teléfono con depuración USB)

## Ejecutar
Con el servidor REST en marcha en el equipo (`http://localhost:5081`):
```bash
dotnet build -t:Run -f net10.0-android
```
- **Emulador:** el equipo se ve como `10.0.2.2`; esa es la dirección por defecto (`MauiProgram.UrlServidorEmulador`).
- **Teléfono real:** en la pantalla de inicio de sesión abrir "Servidor" y escribir `http://IP-DEL-EQUIPO:5081`
  (el servidor debe escucharse en la red: `dotnet run --no-launch-profile --urls http://0.0.0.0:5081`).

Credenciales: `lmoran` / `Eureka2026!`.

## APK ya compilado
`ClienteMovil-GR07.apk` (carpeta `04. CLIMOV`), compilado en Release para `net10.0-android`. Al instalarlo, abrir
"Servidor" en el login y escribir `http://IP-DEL-EQUIPO:5081` (no sirve `localhost` en el teléfono).

Nota: por el límite de 260 caracteres de rutas en Windows, este APK se compiló copiando el proyecto a una ruta
corta (por ejemplo `C:\eub`) antes de ejecutar `dotnet build -f net10.0-android -c Release`.
