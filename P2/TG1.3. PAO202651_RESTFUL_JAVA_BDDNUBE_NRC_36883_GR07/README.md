# TG1.3 – EurekaBank · RESTful en Java con base de datos local (MySQL)

Arquitectura de Software · NRC 36883 · Grupo 7 (Miranda, Morán, Vivanco)

API REST de EurekaBank (Jakarta REST sobre Payara 6, Java 21) que guarda todo en MySQL (`eurekabank`), con cuatro clientes:
consola, escritorio (Swing), web (servlets + HTML/JS) y móvil (Android Java). Es la versión REST/Java de TG1.2 (SOAP .NET con SQL Server):
**mismas operaciones, mismas tablas y mismos datos**; cambian el estilo del servicio (REST/JSON en vez de SOAP/XML) y el motor (MySQL).

## Estructura
| Carpeta | Contenido |
|---|---|
| `04. SERVIDOR/REST_EurekaBank_Java_GR07` | Servidor REST (WAR Maven, contexto `/REST_EurekaBank_Java_GR07`, API en `/api`) |
| `04. CLICON/ClienteConsola` | Cliente de consola (Java 21, Jackson) |
| `04. CLIESC/ClienteEscritorio` | Cliente de escritorio (Swing) |
| `04. CLIWEB/Cliente_Web` | Cliente web (WAR, contexto `/Cliente_Web_EurekaBank_GR07`) |
| `04. CLIMOV/ClienteMovil` | Cliente móvil Android (Java) + APK listo `EurekaBank_REST_Java_GR07.apk` |
| `03. BDD` | Scripts de MySQL: usuario de la aplicación, tablas y datos iniciales |
| `01. UML` | `01. ERS` (requisitos), `02. ECUD` (casos de uso) y `03. DIAGRAMAS` (casos de uso, actividad, secuencia, clases y arquitectura: Power Designer + PNG) |
| `02. MER` | Modelo entidad-relación: conceptual, lógico y físico (Power Designer + PNG) |
| `05. DOCUMENTACION` | Diccionario de datos, manual técnico, manual de usuario, evidencias (capturas) y presentación |

## API REST
| Equivale en TG1.2 | Método y URI | Cuerpo / respuesta |
|---|---|---|
| `iniciarSesion` | `POST /api/sesion/login` | `{"usuario","contrasena"}` → 200 `{"resultado":true}` o 401 |
| `traerCuentasConClientes` | `GET /api/cuentas` | lista de cuentas activas con su cliente |
| `traerMovimientos` | `GET /api/movimientos?cuenta=00100001` | movimientos (sin `cuenta`: todos) |
| `tipoMovimientosPermitidos` | `GET /api/movimientos/tipos` | `["DEPOSITO","RETIRO","TRANSFERENCIA"]` |
| `regMovimiento` | `POST /api/movimientos` | `{"cuentaOrigen","cuentaDestino","tipo","importe"}` → `{"codigo":1,"mensaje":"Operación exitosa"}` |

Estados HTTP: 200 éxito, 400 datos inválidos (o JSON inválido), 401 credenciales incorrectas, 422 regla de negocio
(saldo insuficiente, cuenta inexistente o inactiva, misma cuenta...), 500 error interno. Las transferencias, depósitos y retiros
corren en una sola transacción con bloqueo de filas (`SELECT ... FOR UPDATE`); si algo falla no queda ningún cambio parcial.

## Base de datos
Tablas: `moneda`, `sucursal`, `empleado`, `usuario` (clave con sal + SHA-256), `cliente`, `cuenta`, `tipo_movimiento`,
`movimiento` e `intento_acceso`. Ver `03. BDD/README.txt`.
```
mysql -u root -p < "03. BDD/01_CrearUsuarioApp.sql"
mysql -u root -p < "03. BDD/02_CrearBaseYTablas.sql"
mysql -u root -p < "03. BDD/03_CargaDatos.sql"
```
Usuarios de demostración: `lmoran`, `amiranda`, `gvivanco` · clave `Eureka2026!`.
Cuentas de ejemplo: `00100001`, `00100002`, `00200001`, `00200002`, `00100003`.

## Ejecutar (JDK 21, Maven y Payara 6)
```
cd "04. SERVIDOR/REST_EurekaBank_Java_GR07"
mvn clean package                                   # 13 pruebas (usan H2 con los mismos scripts de 03. BDD)
asadmin start-domain domain1
asadmin create-jvm-options "-Deureka.db.clave=EurekaDb2026!"      # solo la primera vez (clave del usuario eureka_app)
asadmin restart-domain domain1
asadmin deploy --force=true --name REST_EurekaBank_Java_GR07 target/REST_EurekaBank_Java_GR07.war
# http://localhost:8080/REST_EurekaBank_Java_GR07/        (portada)   http://localhost:8080/REST_EurekaBank_Java_GR07/api/cuentas

cd "04. CLICON/ClienteConsola"      && mvn clean compile dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
java -cp "target/classes;target/dependency/*" ec.edu.monster.clienteconsola.ClienteConsola      # [URL del servidor]
cd "04. CLIESC/ClienteEscritorio"   && (igual que la consola) java -cp "target/classes;target/dependency/*" ec.edu.monster.clienteescritorio.ClienteEscritorio
cd "04. CLIWEB/Cliente_Web"         && mvn clean package && asadmin deploy --force=true --name Cliente_Web_EurekaBank_GR07 target/Cliente_Web_EurekaBank_GR07.war
# http://localhost:8080/Cliente_Web_EurekaBank_GR07/
```
Para usar otro servidor: `EUREKA_URL` / `-Deureka.url` (consola y escritorio) o el campo «Servidor» del login
(`http://IP:8080/REST_EurekaBank_Java_GR07/api/`). Para que otros equipos se conecten hay que abrir el puerto 8080 en el firewall.

## Móvil
APK listo: `04. CLIMOV/EurekaBank_REST_Java_GR07.apk`. En el login, abrir «Servidor» y escribir
`http://IP_DEL_PC:8080/REST_EurekaBank_Java_GR07/api/` (en el emulador es `10.0.2.2`).
Compilar: `gradlew.bat assembleDebug` (JDK 17+ y Android SDK 34).


## Archivos en la raíz
- `POWER DESIGNER/`: modelos de Power Designer (`MER` y `UML`).
