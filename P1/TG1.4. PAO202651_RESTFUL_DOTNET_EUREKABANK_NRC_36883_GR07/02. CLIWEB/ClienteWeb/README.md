# ClienteWeb - EurekaBank (GR07, .NET)

Cliente web (Blazor Server, .NET 10) del servicio REST `API_EurekaBank_RESTDOTNET_GR07`. El navegador nunca habla
REST directamente: el servidor web llama al servicio REST y mantiene la sesión en el circuito de cada usuario.

La interfaz (login y panel de cuentas, movimientos y operaciones) está en la biblioteca compartida
`02. CLIESC/EurekaBankUI.Shared`, la misma que usan el cliente de escritorio y el móvil: un único diseño en
los tres (RNF-006).

## Ejecutar
Con el servidor REST en marcha (`http://localhost:5081`):
```bash
dotnet run --no-launch-profile      # http://localhost:5090
```
Dirección del servicio REST: variable de entorno `EUREKA_URL` (por defecto `http://localhost:5081`).

## Uso
1. Iniciar sesión (usuario `lmoran`, clave `Eureka2026!`).
2. Ver cuentas, consultar movimientos (por cuenta o todas) y registrar depósitos, retiros o transferencias.
