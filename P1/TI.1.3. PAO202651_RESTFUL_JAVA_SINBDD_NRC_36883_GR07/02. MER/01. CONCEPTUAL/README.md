# Modelo conceptual

El Usuario utiliza la API para autenticar una sesión y pedir conversiones. Una sesión contiene un token temporal. El Catálogo agrupa categorías y unidades; cada categoría tiene un conversor. La Solicitud de conversión referencia dos unidades que deben pertenecer a la misma categoría y produce una Respuesta.

Estas relaciones representan conceptos del dominio en ejecución; no implican tablas, claves foráneas ni persistencia.
