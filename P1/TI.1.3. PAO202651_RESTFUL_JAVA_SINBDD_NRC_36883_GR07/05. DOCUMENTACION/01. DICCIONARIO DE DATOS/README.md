# Diccionario de datos

## Solicitudes

| Campo | Tipo JSON | Uso |
|---|---|---|
| `usuario` | cadena | Nombre de usuario para login |
| `clave` | cadena | Clave de login |
| `valor` | número | Magnitud a convertir; finito |
| `unidadOrigen` | cadena | Unidad de entrada |
| `unidadDestino` | cadena | Unidad de salida |
| `categoria` | segmento de ruta | `Longitud`, `Masa` o `Temperatura` |
| `Authorization` | cabecera | `Bearer <token>` en recursos protegidos |

## Respuesta común

| Campo | Tipo JSON | Descripción |
|---|---|---|
| `estado` | cadena | `OK` o `ERROR` |
| `codigo` | cadena | Código funcional (`OK-200`, `AUTH-001`, `AUTH-002`, `AUTH-003`, `UNIT-001`, `DATA-001`, `SRV-500`) |
| `mensaje` | cadena | Explicación legible |
| `resultado` | número o null | Resultado de conversión |
| `unidadOrigen` | cadena o null | Unidad normalizada de entrada |
| `unidadDestino` | cadena o null | Unidad normalizada de salida |
| `token` | cadena o null | Token emitido en login |
| `datos` | arreglo de cadenas | Categorías o unidades |

Estados HTTP: 200 correcto, 400 datos/categoría inconsistentes, 401 autenticación, 422 unidad no soportada y 500 error inesperado.
