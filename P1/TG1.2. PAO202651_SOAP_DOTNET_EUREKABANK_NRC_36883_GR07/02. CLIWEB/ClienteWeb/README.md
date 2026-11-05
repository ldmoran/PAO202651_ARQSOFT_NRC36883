# ClienteWeb - EurekaBank (GR07, .NET)

Cliente web (Blazor Server, .NET 10) del servicio SOAP `WS_ConUni_DotNet_GR07`. El navegador nunca habla SOAP ni
conoce el token: el servidor web llama al servicio SOAP y guarda el token en la sesión de cada usuario.

La interfaz (login y conversor) está en la biblioteca compartida `04. CLIESC/EurekaBankUI.Shared`, la misma
que usan el cliente de escritorio y el móvil: un único diseño en los tres (RNF-006).

## Ejecutar
Con el servidor SOAP en marcha (`http://localhost:5081/WSEurekaBank`):
```bash
dotnet run --no-launch-profile      # http://localhost:5090
```
Dirección del servicio SOAP: variable de entorno `EUREKA_URL` (por defecto `http://localhost:5081/WSEurekaBank`).

## Uso
1. Iniciar sesión (usuario `monster`, clave `monster9`).
2. Elegir categoría, unidades, escribir el valor (punto o coma decimal) y pulsar "Convertir".
3. Si el token es inválido o la sesión expiró, el cliente vuelve al login.
