# Modelos UML para importar en PowerDesigner

Estos archivos son modelos de intercambio XMI 2.1 con elementos y relaciones UML. No contienen la disposición gráfica de los diagramas y todavía no son archivos nativos `.oom` ni imágenes `.png`.

| Archivo | Contenido |
|---|---|
| `CasosUso_TI13.xmi` | Actor Usuario, casos de uso y relaciones include |
| `ActividadConversion_TI13.xmi` | Flujo autenticado de conversión y respuestas alternativas |
| `SecuenciaConversion_TI13.xmi` | Interacción entre usuario, cliente y componentes REST |
| `Clases_TI13.xmi` | Recursos, manejadores, seguridad, catálogo, conversores y respuestas |
| `Arquitectura_TI13.xmi` | Clientes, API REST en Payara, capas y estado en memoria |

## Conversión pendiente

PowerDesigner 16.5 incluye el importador XMI, pero la automatización local no inicia mientras la licencia esté vencida. Cuando esté habilitada:

1. Importa cada archivo XMI desde la opción de importación XMI de PowerDesigner.
2. Revisa los elementos importados y crea/ordena la vista gráfica correspondiente.
3. Guarda cada modelo nativo con extensión `.oom`.
4. Exporta cada diagrama a `.png`.

La exportación a `.oom` y `.png` no se simuló ni se declara terminada. Los modelos XMI se validaron como XML y se comprobó la unicidad de IDs y la resolución de referencias internas.
