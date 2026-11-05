# Clientes de escritorio y compartido - EurekaBank (.NET)

- **EurekaBankUI.Shared**: biblioteca Razor con la interfaz (login y panel de cuentas, movimientos y operaciones) y el cliente SOAP (SoapClient). La usan el escritorio, la web y el movil: un solo diseno.
- **ClienteEscritorio**: ventana WPF con BlazorWebView que aloja esa interfaz.

Ejecutar (con el servidor en marcha):

    cd ClienteEscritorio
    dotnet run

Otro servidor: variable EUREKA_URL o el campo "Servidor" del login (http://IP:5081/WSEurekaBank).
