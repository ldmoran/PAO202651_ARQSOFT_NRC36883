# Examen complexivo práctico · BanQuito, módulo de crédito (GR07)

SOAP + Java + MySQL. Dos aplicaciones web en Payara (Banco Core y Comercializadora) y cuatro clientes (consola, escritorio, web, móvil).

| Carpeta | Contenido |
|---|---|
| `01. UML` | `02. ECUD` (casos de uso) y `03. DIAGRAMAS` (casos de uso, secuencia, arquitectura y documento) |
| `02. MER` | Modelos conceptual, lógico y físico del banco y de la comercializadora |
| `03. BDD` | Scripts MySQL: usuario de la aplicación, creación y carga; `Actualizar_DetalleFactura.sql` para bases antiguas |
| `04. SERVIDOR` | `BanQuitoCore` (`WSCredito`) y `Comercializadora` (`WSComercializadora`) |
| `04. CLICON` | `ClienteBase` (cliente SOAP común) y `ClienteConsola` |
| `04. CLIESC` / `04. CLIWEB` | `ClienteEscritorio` (Swing) / `ClienteWeb` (servlet) |
| `04. CLIMOV` | Cliente Android (Gradle) |
| `05. DOCUMENTACION` | Diccionario de datos, manual técnico (WS), manual de usuario, evidencias, presentación y enunciado |
| `POWER DESIGNER` | Modelos de Power Designer (`MER` y `UML`) |

## Puesta en marcha
1. MySQL: `03. BDD/CrearUsuarioApp.sql` como administrador, luego `CreaBD_Banquito.sql`, `CargaDatos_Banquito.sql`, `CreaBD_Comercializadora.sql` y `CargaDatos_Comercializadora.sql`.
2. Servidores: `mvn clean package` en cada carpeta de `04. SERVIDOR` y desplegar los `.war` en Payara. La clave de MySQL va fuera del código: `asadmin create-jvm-options "-Dbanquito.db.clave=..."` y `"-Dcomercializadora.db.clave=..."`.
3. Clientes: `mvn clean package` en la raíz; consola y escritorio son `java -jar`, la web es `ClienteWeb.war`; el móvil con `gradlew assembleDebug` en `04. CLIMOV/ClienteMovil`.
4. Equipos distintos: banco → `-Dcomercializadora.banco.url=http://IP-BANCO:8080/BanQuitoCore/WSCredito`; clientes → IP de la Comercializadora (argumento, `COMERCIALIZADORA_URL`, `web.xml` o botón *Servidor* en el móvil).

## Reglas
- Sujeto de crédito: cliente del banco, con depósito en el último mes, sin crédito activo y no hombre menor de 25 años.
- Monto máximo: `((prom. depósitos - prom. retiros) x 30 %) x 6` (últimos 3 meses). Crédito: 16,5 % anual, 3 a 18 meses, cuota fija.
- Efectivo: 42 % de descuento. Una compra con varios celulares sale en **una sola factura** (una línea por modelo); a crédito, un solo crédito por el total.

## Pruebas
`mvn test` en `BanQuitoCore` (29), `Comercializadora` (16) y en la raíz de clientes (8).
