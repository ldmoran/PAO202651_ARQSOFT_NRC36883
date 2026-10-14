# ClienteMovil - Conversión de Unidades Monster (GR07)

Cliente móvil (Android, Java) del servicio SOAP `WS_ConUni_Java_GR07`. No contiene lógica de
conversión: todo el cálculo lo hace el servidor. No usa librerías externas (solo clases estándar
de Android y Java).

## Requisitos
- Android Studio (Ladybug o superior) con Android SDK 34 y Build Tools 35 (Android Studio los
  ofrece instalar al abrir el proyecto)
- El servidor desplegado (ver `04. SERVIDOR/WS_ConUni_Java_GR07/README.md`)

## Ejecutar
1. Abrir la carpeta `ClienteMovil` en Android Studio y esperar la sincronización de Gradle.
2. Ejecutar la app en un emulador o en un celular.

## Servidor
- En el **emulador**, el equipo anfitrión se ve como `10.0.2.2`; por eso la URL por defecto es
  `http://10.0.2.2:8080/WS_ConUni_Java_GR07/`.
- En un **celular real**, escribir en el campo "Servidor" de la pantalla de login la IP del equipo
  donde corre Payara, por ejemplo `http://192.168.1.50:8080/WS_ConUni_Java_GR07/` (celular y equipo
  en la misma red). La URL queda guardada para la próxima vez.

## Uso
1. Login: usuario `monster`, clave `monster9`.
2. Pantalla de conversión: categoría, unidades de origen y destino, valor (punto o coma decimal) y
   "Convertir". Las categorías y unidades se piden al servidor.
3. Si el token es inválido o la sesión expiró, la app avisa y vuelve al login.

## Estructura
- `modelo/`: `SoapClient` (mensajes SOAP con `HttpURLConnection`) y `RespuestaServicio`.
- `controlador/`: `ControladorConversiones` (token y llamadas) y `SesionApp` (comparte la sesión
  entre pantallas).
- `vista/`: `LoginActivity` y `ConversionActivity`.
- `res/`: layouts, colores y la imagen del tema Monster (la misma de la carátula del ERS).

## Pruebas
`app/src/test/.../SoapClientTest.java` (JUnit 4, servidor falso, no necesita emulador):
`./gradlew test`.
