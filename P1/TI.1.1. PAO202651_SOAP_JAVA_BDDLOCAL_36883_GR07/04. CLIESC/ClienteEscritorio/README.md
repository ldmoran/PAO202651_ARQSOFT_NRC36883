# ClienteEscritorio - Conversión de Unidades Monster (GR07)

Cliente de escritorio (Java Swing) del servicio SOAP `WS_ConUni_Java_GR07`. No contiene lógica
de conversión: todo el cálculo lo hace el servidor.

## Requisitos
- JDK 21 y Maven
- El servidor desplegado (ver `04. SERVIDOR/WS_ConUni_Java_GR07/README.md`)

## Ejecutar
```bash
mvn compile
java -cp target/classes ec.edu.monster.clienteescritorio.ClienteEscritorio
```
(o ejecutar `ClienteEscritorio.java` desde NetBeans).

## Uso
1. Ventana de login: usuario `monster`, clave `monster9`. El campo "Servidor" trae la URL por
   defecto (`http://localhost:8080/WS_ConUni_Java_GR07/`); se puede cambiar.
2. Ventana de conversión: elegir categoría, unidades de origen y destino, escribir el valor y
   pulsar "Convertir". Acepta punto o coma decimal.
3. Las categorías y unidades se piden al servidor (catálogo), no están escritas en el cliente.
4. Si el token es inválido o la sesión expiró, el cliente avisa y vuelve al login.

## Estructura (MVC)
- `modelo/`: `SoapClient` y `RespuestaServicio`.
- `controlador/ControladorConversiones`: guarda el token y llama al servicio.
- `vista/`: `LoginFrame`, `ConversionFrame` y `Estilo` (misma línea gráfica que el resto de clientes).

## Pruebas
`mvn test` (8 pruebas del modelo con un servidor falso).
