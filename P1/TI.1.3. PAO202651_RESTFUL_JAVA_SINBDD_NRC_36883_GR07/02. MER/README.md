# Modelo Entidad-Relación - TI.1.3

No se implementa una base de datos, por lo que no existen tablas ni script de creación. El modelo conceptual de este directorio describe objetos transitorios en memoria, no entidades persistentes.

| Concepto | Atributos principales | Persistencia |
|---|---|---|
| Usuario de práctica | usuario, clave | Constante de configuración en memoria |
| Sesión | token, fecha de expiración | Mapa concurrente en memoria |
| Categoría | nombre, conjunto de conversores | Catálogo en memoria |
| Unidad | nombre normalizado, categoría | Lista/mapa en memoria |
| Solicitud de conversión | valor, unidadOrigen, unidadDestino, categoría | Solo durante la petición |
| Respuesta | estado, código, mensaje, resultado, datos | Solo durante la petición |

Los diagramas conceptual, lógico y físico no describen almacenamiento; la carpeta conserva su organización para documentar explícitamente la ausencia de BDD.
