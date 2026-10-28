# ClienteEscritorio - Conversión de Unidades Monster (GR07)

Cliente de escritorio Java Swing que consume la API REST JSON de `REST_ConUni_Java_GR07`. Las conversiones se ejecutan en el servidor.

## Requisitos y ejecución

- JDK 21, Maven 3.9+
- Servidor REST desplegado (ver [README del servidor](../REST_ConUni_Java_GR07/README.md))

```bash
mvn clean test
mvn compile
```

Ejecuta `ec.edu.monster.clienteescritorio.ClienteEscritorio` desde NetBeans o el IDE. En el login se puede editar el URL; por defecto es `http://localhost:8080/REST_ConUni_Java_GR07/api/`.

## Uso y arquitectura

Inicia sesión con `monster` / `monster9`; la ventana de conversión carga el catálogo del servidor, permite seleccionar categoría y unidades, y acepta punto o coma decimal. Las llamadas protegidas llevan el token como `Bearer`; al expirar, vuelve al login.

La aplicación mantiene MVC: cliente REST y respuesta en `modelo/`, coordinación de sesión en `controlador/`, ventanas Swing en `vista/`.

## Pruebas

`mvn test` usa un servidor HTTP falso y no necesita Payara ni el servidor real.
