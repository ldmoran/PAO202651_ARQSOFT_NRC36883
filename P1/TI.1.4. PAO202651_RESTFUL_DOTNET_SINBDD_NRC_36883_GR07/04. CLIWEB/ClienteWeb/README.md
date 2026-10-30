# ClienteWeb - Conversión de Unidades Monster (GR07, .NET, REST)

Cliente web (Blazor Server, .NET 10) de la API REST `API_ConUni_GR07`. El navegador nunca habla
directamente con la API ni conoce el token: el servidor web llama a la API REST y guarda el
token en la sesión (circuito) de cada usuario.

La interfaz (login y conversor) está en la biblioteca compartida `04. CLIESC/SistemaConversorUI.Shared`, la misma
que usan el cliente de escritorio y el móvil: un único diseño en los tres (RNF-006).

## Ejecutar
Con la API REST en marcha (`http://localhost:5080/`):
```bash
dotnet run --no-launch-profile      # http://localhost:5090
```
Dirección de la API REST: variable de entorno `CONUNI_URL` (por defecto `http://localhost:5080/`).

## Uso
1. Iniciar sesión (usuario `monster`, clave `monster9`).
2. Elegir categoría, unidades, escribir el valor (punto o coma decimal) y pulsar "Convertir".
3. Si el token es inválido o la sesión expiró, el cliente vuelve al login.
