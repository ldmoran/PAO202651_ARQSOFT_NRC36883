# Guion de presentación - TI.1.3

1. **Problema y alcance:** conversión de longitud, masa y temperatura desde cuatro clientes.
2. **Arquitectura:** clientes Java -> API REST JSON -> manejadores -> catálogo/conversores; autenticación y sesiones en memoria.
3. **Contrato:** login, consultas de catálogo y conversiones; token Bearer para recursos protegidos.
4. **Demostración:** iniciar sesión, consultar unidades y convertir en consola/escritorio/web/móvil.
5. **Calidad:** validaciones, códigos HTTP/funcionales y pruebas unitarias contra servidor HTTP falso.
6. **Restricción deliberada:** sin base de datos; tokens volátiles con expiración de 30 minutos.
7. **Cierre:** despliegue WAR en Payara/GlassFish y rutas de documentación.
