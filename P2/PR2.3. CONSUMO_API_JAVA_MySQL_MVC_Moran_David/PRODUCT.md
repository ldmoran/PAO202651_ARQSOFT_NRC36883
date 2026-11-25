# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack
Web con HTML, CSS y JS propios servidos por el propio servidor (HTML generado por el servlet en Java; vistas Razor en ASP.NET Core MVC para la versión .NET). Segunda superficie: app Android nativa en Java (como la de BanQuito). La base de datos es local (MySQL en Java, SQL Server en .NET) y se puede apuntar a AWS RDS con variables de entorno.

## Users
Ing. Campaña (docente de Arquitectura de Software, ESPE) y compañeros, durante la exposición del taller. El trabajo es individual: David Moran, único autor y responsable. Usan las pantallas para importar países desde una API pública, buscarlos, editarlos y ver reportes.

## Product Purpose
Taller práctico "Consumo de APIs Públicas" con patrón MVC: consumir la API REST Countries (https://restcountries.com/v3.1/all), guardar los países en una base de datos propia y consultarlos sin depender del servicio externo. Éxito: que el taller se vea y funcione impecable en la demo, con las cinco actividades cumplidas y sin necesidad de explicación.

## Positioning
Es un cliente web y móvil de datos de países con identidad propia (tema atlas: papel de mapa, tinta de océano, verde mar y terracota) sobre una arquitectura MVC real, no una plantilla genérica de tabla con botones.

## Operating Context
Demostración en clase y entrega con manual, presentación y diagramas en el formato del grupo. Servidor y base de datos en el equipo del autor; el celular o el emulador se conectan por red local o por 10.0.2.2.

## Capabilities and Constraints
- Importar todos los países desde REST Countries y guardarlos (nombre, capital, región, subregión, población, área, moneda).
- Consultar un país por nombre (ej. /v3.1/name/ecuador).
- CRUD completo de países.
- Búsqueda por nombre, región y capital; filtros por región (América, Europa, Asia, África, Oceanía).
- Reportes: países por región, país más poblado, promedio de población por región.
- Versión .NET: además API de universidades (universities.hipolabs.com) con entidad Universidad.
- Restricciones de estilo ya decididas por el usuario: tema atlas, sin emojis como íconos, sin texto con degradado, sin efectos de vidrio. Las contraseñas de bases de datos nunca se escriben en archivos del repositorio.

## Brand Commitments
Tema "atlas" (cartografía): fondo de papel cálido, panel lateral azul océano con curvas de nivel, acento verde mar y terracota, títulos en serifa. Sin personajes ni material con derechos de autor.

## Evidence on Hand
Los dos talleres de referencia (Google Docs) con el código y las actividades.

## Product Principles
1. La demo manda: cada pantalla debe entenderse y verse bien proyectada, sin explicación.
2. MVC visible: la interfaz solo presenta; las reglas viven en el servidor.
3. Datos reales: nada simulado en pantalla; los números salen de la base.
4. Una identidad (atlas) coherente entre web y Android.
5. Rápido y liviano: sin dependencias pesadas.

## Accessibility & Inclusion
Contraste suficiente sobre el panel azul océano, foco visible por teclado, objetivos táctiles de al menos 44 px en Android, y movimiento reducido cuando el sistema lo pida.
