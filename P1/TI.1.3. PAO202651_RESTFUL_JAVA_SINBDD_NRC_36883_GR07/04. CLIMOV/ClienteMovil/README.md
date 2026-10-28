# ClienteMovil - Conversión de Unidades Monster (GR07)

Cliente Android Java de la API REST JSON `REST_ConUni_Java_GR07`. La lógica de conversión permanece en el servidor.

## Requisitos y ejecución

- Android Studio con JDK 17, Android SDK 34 y Build Tools 35
- Servidor REST desplegado (ver [README del servidor](../REST_ConUni_Java_GR07/README.md))

Abre `ClienteMovil` en Android Studio, sincroniza Gradle y ejecuta en emulador o dispositivo.

## Dirección del servidor

- En emulador, `10.0.2.2` apunta al host: `http://10.0.2.2:8080/REST_ConUni_Java_GR07/api/`.
- En dispositivo real, configura en login `http://<IP-del-host>:8080/REST_ConUni_Java_GR07/api/` y conecta ambos equipos a la misma red.

## Uso y arquitectura

Inicia sesión con `monster` / `monster9`, selecciona categoría y unidades consultadas al servidor, escribe el valor y convierte. El cliente guarda el token durante la sesión abierta y lo envía como Bearer; una respuesta de token inválido/expirado devuelve a login.

`modelo/` contiene `RestClient` (HTTP/JSON con `HttpURLConnection`) y la respuesta estructurada; `controlador/` gestiona el token y lo comparte entre pantallas; `vista/` contiene las Activities y layouts.

## Pruebas

`./gradlew test` ejecuta las pruebas unitarias con servidor HTTP falso; no requiere emulador ni Payara.
