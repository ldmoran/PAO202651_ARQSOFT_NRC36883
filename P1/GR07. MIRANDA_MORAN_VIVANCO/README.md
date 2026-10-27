# Examen complexivo práctico - BanQuito, módulo de crédito (GR07)

Opción elegida: **SOAP + Java + MySQL**. Dos aplicaciones web independientes en Payara y cuatro clientes.

| Carpeta | Contenido |
|---|---|
| `01. SERVIDOR/BanQuitoCore` | Banco Core: servicio `WSCredito` (base `banquito`) |
| `01. SERVIDOR/Comercializadora` | Comercializadora: servicio `WSComercializadora` (base `comercializadora`), habla con el banco por SOAP |
| `02. CLIENTES` | Maven: `ClienteBase` (cliente SOAP común), `ClienteConsola`, `ClienteEscritorio` (Swing), `ClienteWeb` |
| `02. CLIMOV/ClienteMovil` | Cliente Android (Gradle) |
| `03. BD` | Scripts MySQL de creación, carga y usuario de la aplicación |
| `04. DOCUMENTACION` | ECUD, MER, BD, DIAGRAMAS y WS en Word; `DIAGRAMAS/PowerDesigner` con los modelos de Power Designer (.oom, .cdm, .ldm, .pdm), imágenes exportadas en `DIAGRAMAS` y capturas en `EVIDENCIAS` |

## Puesta en marcha

1. MySQL: ejecutar `03. BD/CrearUsuarioApp.sql` como administrador y luego `CreaBD_Banquito.sql`, `CargaDatos_Banquito.sql`,
   `CreaBD_Comercializadora.sql` y `CargaDatos_Comercializadora.sql`.
2. Servidores: `mvn clean package` en cada carpeta de `01. SERVIDOR` y desplegar los `.war` en Payara.
   La clave de MySQL no está en el código: `asadmin create-jvm-options "-Dbanquito.db.clave=..."` y `"-Dcomercializadora.db.clave=..."`.
3. Clientes: `mvn clean package` en `02. CLIENTES`; consola y escritorio son `java -jar`, la web es `ClienteWeb.war`;
   el móvil se abre en Android Studio o con `gradlew assembleDebug`.
4. Entre equipos distintos: banco -> `-Dcomercializadora.banco.url=http://IP-BANCO:8080/BanQuitoCore/WSCredito`;
   clientes -> IP de la Comercializadora (argumento, `COMERCIALIZADORA_URL`, `web.xml` o botón *Servidor* en el móvil).

## Reglas implementadas

- Sujeto de crédito: cliente del banco, al menos un depósito en el último mes, sin crédito activo y no ser hombre menor de 25 años.
- Monto máximo: `((prom. depósitos - prom. retiros) x 30 %) x 6` con los últimos 3 meses (el enunciado menciona también 35 %; ver `ECUD`).
- Crédito: tasa 16,5 % anual, plazo 3 a 18 meses, cuota fija; la última cuota ajusta centavos para que el saldo final sea 0,00.
- Efectivo: 42 % de descuento calculado en la Comercializadora. Crédito: aprobado si el precio no supera el monto máximo.

## Pruebas

`mvn test` en `BanQuitoCore` (29), `Comercializadora` (10) y `02. CLIENTES` (8).
