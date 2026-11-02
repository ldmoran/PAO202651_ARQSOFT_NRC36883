# Manual de usuario

## Antes de comenzar

El servidor REST debe estar desplegado. El URL predeterminado es `http://localhost:8080/REST_ConUni_Java_GR07/api/`. En un Android emulado se usa `http://10.0.2.2:8080/REST_ConUni_Java_GR07/api/`.

## Iniciar y convertir

1. Abre consola, escritorio, navegador o app móvil.
2. Si el cliente lo solicita, confirma la URL del servidor.
3. Inicia sesión con `monster` y `monster9`.
4. Selecciona categoría y unidades, escribe el valor y solicita la conversión.
5. Usa “Cerrar sesión” al terminar.

Las categorías y unidades se consultan al servidor; no hay que registrarlas localmente.

## Mensajes comunes

- `AUTH-001` / `AUTH-002`: token inválido o sesión vencida; vuelve a iniciar sesión.
- `AUTH-003`: revisa usuario y clave.
- `UNIT-001`: selecciona una unidad soportada.
- `DATA-001`: revisa el valor, categoría y unidades elegidas.
- `NET-001`: el cliente no se conectó; confirma URL, red y estado del servidor.

## Diferencias por plataforma

- **Consola:** usa el menú para login, catálogo, conversión y cierre.
- **Escritorio:** el campo Servidor de login permite indicar el URL.
- **Web:** los servlets protegen el token en sesión HttpOnly.
- **Móvil:** emulador Android usa `10.0.2.2`; en un dispositivo real configura la IP local del host.
