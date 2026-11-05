# TG1.1 – EurekaBank · SOAP en Java con base de datos local (MySQL)

Arquitectura de Software · NRC 36883 · Grupo 7 (Miranda, Morán, Vivanco)

Servicio SOAP de EurekaBank (Java, Jakarta EE 10 / JAX-WS) desplegado en Payara, que guarda todo en MySQL (`eurekabank`), con cuatro
clientes: consola, escritorio, web y móvil. El contrato SOAP (espacio de nombres y operaciones) es el mismo del servidor .NET (TG1.2),
así que **los mismos clientes sirven con los dos servidores cambiando solo la URL**.

## Estructura
| Carpeta | Contenido |
|---|---|
| `04. SERVIDOR/WS_EurekaBank_SOAPJAVA_GR07` | Servidor SOAP (WAR para Payara, contexto `/WS_EurekaBank_SOAPJAVA_GR07`) |
| `04. CLICON` | `ClienteBase` (cliente SOAP común) y `ClienteConsola` |
| `04. CLIESC/ClienteEscritorio` | Cliente de escritorio (Swing + FlatLaf) |
| `04. CLIWEB/ClienteWeb` | Cliente web (WAR) |
| `04. CLIMOV/ClienteMovil` | Cliente móvil Android (Gradle, Java) |
| `03. BDD` | Scripts de MySQL: usuario de la aplicación, tablas y datos iniciales |
| `01. UML` | `01. ERS` (requisitos), `02. ECUD` (casos de uso) y `03. DIAGRAMAS` (casos de uso, actividad, secuencia, clases y arquitectura: Power Designer + PNG) |
| `02. MER` | Modelo entidad-relación: conceptual, lógico y físico (Power Designer + PNG) |
| `05. DOCUMENTACION` | Diccionario de datos, manual técnico, manual de usuario, evidencias (capturas) y presentación |

## Base de datos (MySQL 8)
```bash
mysql -u root -p < "03. BDD/01_CrearUsuarioApp.sql"        # una vez, como administrador (crea eurekabank y eureka_app)
mysql -u eureka_app -p < "03. BDD/02_CrearTablas.sql"
mysql -u eureka_app -p < "03. BDD/03_CargaDatos.sql"
```
Usuario de la aplicación: `eureka_app` (clave en el script; cámbiela si quiere). Usuarios de demostración: `lmoran`, `amiranda`,
`gvivanco` · clave `Eureka2026!`. Las claves se guardan con sal + SHA-256.

## Servidor
```bash
cd "04. SERVIDOR/WS_EurekaBank_SOAPJAVA_GR07"
mvn clean package                       # 27 pruebas + WAR
asadmin create-jvm-options "-Deureka.db.clave=LA_CLAVE_DE_eureka_app"   # una vez (Payara)
asadmin deploy --force=true target/WS_EurekaBank_SOAPJAVA_GR07.war
```
WSDL: `http://localhost:8080/WS_EurekaBank_SOAPJAVA_GR07/WSEurekaBank?wsdl`

## Clientes
```bash
mvn clean package -DskipTests            # desde la raíz del proyecto (pom.xml)
java -jar "04. CLICON/ClienteConsola/target/ClienteConsola.jar"                 # EUREKA_URL o argumento 1 = URL del servicio
java -jar "04. CLIESC/ClienteEscritorio/target/ClienteEscritorio.jar"
asadmin deploy --force=true "04. CLIWEB/ClienteWeb/target/ClienteWeb.war"       # http://localhost:8080/CLIWEB_EuBank_SOAPJAVA_GR07
```
Móvil: instalar `EurekaBank_SOAP_Java_GR07.apk`; en el engranaje de la cabecera escribir
`http://IP_DEL_PC:8080/WS_EurekaBank_SOAPJAVA_GR07/WSEurekaBank` (en el emulador, `10.0.2.2`).
Para compilarlo: `./gradlew assembleDebug` con JDK 21 (y `local.properties` con `sdk.dir`).

## Archivos en la raíz y pruebas
- `POWER DESIGNER/`: modelos de Power Designer (`MER/*.cdm, *.ldm, *.pdm` y `UML/*.oom`).
- `APK`: `04. CLIMOV/EurekaBank_SOAP_Java_GR07.apk` (en el celular, abrir el engranaje de la cabecera y escribir la dirección del servicio).
