# ClienteConsola - Conversión de Unidades Monster (GR07)

Cliente de consola (Java) del servicio SOAP `WS_ConUni_Java_GR07`. No contiene lógica de
conversión: todo el cálculo lo hace el servidor.

## Requisitos
- JDK 21 y Maven
- El servidor desplegado (ver `04. SERVIDOR/WS_ConUni_Java_GR07/README.md`)

## Ejecutar
```bash
mvn compile
java -cp target/classes ec.edu.monster.clienteconsola.ClienteConsola
```
(o ejecutar `ClienteConsola.java` desde NetBeans).

URL del servidor, en este orden: argumento 1, propiedad `-Dconuni.url=...`, variable de entorno
`CONUNI_URL`, o `http://localhost:8080/WS_ConUni_Java_GR07/` por defecto.

En Windows, si los acentos se ven mal: `chcp 65001` antes de ejecutar.

## Uso
1. Opción 1: iniciar sesión (usuario `monster`, clave `monster9`).
2. Con la sesión activa aparecen: 2 (catálogo), 3 (convertir), 4 (cerrar sesión).
3. Si el servidor responde token inválido o sesión expirada, el cliente cierra la sesión local y
   pide iniciar sesión otra vez.

## Estructura (MVC)
- `modelo/`: `SoapClient` (arma y envía los mensajes SOAP), `RespuestaServicio` y `Sesion` (guarda el token).
- `controlador/`: `ControladorSesion` (iniciar y cerrar sesión) y `ControladorConversiones` (catálogo y conversiones;
  cierra la sesión local si el servidor rechaza el token).
- `vista/`: `InterfazConsola` (menú principal), `VistaSesion`, `VistaConversion` y `ConsolaIO` (marcos y lectura validada).
- `ClienteConsola`: arma las piezas (modelo, controladores y vistas) y arranca el menú.

## Pruebas
`mvn test` (10 pruebas con un servidor falso, no necesita el servidor real).
