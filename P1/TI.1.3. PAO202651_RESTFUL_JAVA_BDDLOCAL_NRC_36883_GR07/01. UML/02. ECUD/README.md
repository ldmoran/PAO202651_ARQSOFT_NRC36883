# Especificación de casos de uso - TI.1.3

La especificación está en `ECUD_TI_1.3_GR07.docx`. El diagrama de casos de uso se elaboró en PowerDesigner y se incluye en el documento; sus archivos editables y PNG están en `../03. DIAGRAMAS/01_CASOS_USO`.

| Caso | Nombre | Precondición | Resultado |
|---|---|---|---|
| CU-01 | Iniciar sesión | API disponible | Token de sesión o error de credenciales |
| CU-02 | Consultar categorías | Token válido | Lista de categorías |
| CU-03 | Consultar unidades | Token válido y categoría existente | Lista de unidades |
| CU-04 | Convertir unidades | Token válido y unidades compatibles | Resultado numérico |
| CU-05 | Validar token | Solicitud protegida | Operación continúa o error AUTH |
| CU-06 | Validar datos | Solicitud recibida | Operación continúa o error DATA/UNIT |
| CU-07 | Entregar respuesta | Operación procesada | JSON común y estado HTTP |

## Flujo principal de CU-04

1. El usuario elige categoría, unidad de origen, unidad de destino e ingresa el valor.
2. El cliente envía `POST /api/conversiones/{categoria}` con JSON y token Bearer.
3. La API valida token, valor, unidades y pertenencia a la categoría.
4. El conversor de la categoría calcula el resultado.
5. La API devuelve HTTP 200 con `OK-200` y el resultado.

## Flujos alternativos

- Token inválido o vencido: HTTP 401 con `AUTH-001` o `AUTH-002`; el cliente elimina la sesión local.
- Unidad inexistente: HTTP 422 con `UNIT-001`.
- Valor faltante, categoría desconocida o unidades de categorías distintas: HTTP 400 con `DATA-001`.
- Error no esperado del servidor: HTTP 500 con `SRV-500`.
