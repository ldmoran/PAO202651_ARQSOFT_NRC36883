# Modelo lógico

El servidor organiza el dominio en objetos Java:

- `GestorTokens`: mapa concurrente `token -> vencimiento`.
- `CatalogoUnidades`: lista de implementaciones `Conversor`.
- `ConversorLongitud`, `ConversorMasa`, `ConversorTemperatura`: unidades y fórmulas.
- `SolicitudLogin`, `SolicitudConversion`: DTO de entrada JSON.
- `RespuestaServicio`: DTO común de salida.

No se generan tablas ni identificadores persistentes. Los datos se reinician al apagar o redeplegar el servidor.
