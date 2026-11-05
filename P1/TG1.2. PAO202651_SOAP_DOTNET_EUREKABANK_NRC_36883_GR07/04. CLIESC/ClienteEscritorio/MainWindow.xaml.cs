using System.Windows;
using ec.edu.monster.Cliente.Servicios;
using Microsoft.Extensions.DependencyInjection;

namespace ClienteEscritorio
{
    /// <summary>
    /// Ventana del cliente de escritorio. Aloja la misma interfaz Blazor (EurekaBankUI.Shared)
    /// que usan el cliente web y el movil, para mantener un unico diseno (RNF-006).
    /// URL del servicio SOAP: variable EUREKA_URL o http://localhost:5081/WSEurekaBank
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            var servicios = new ServiceCollection();
            servicios.AddWpfBlazorWebView();
            servicios.AgregarClienteEureka();
            blazorWebView.Services = servicios.BuildServiceProvider();
        }
    }
}
