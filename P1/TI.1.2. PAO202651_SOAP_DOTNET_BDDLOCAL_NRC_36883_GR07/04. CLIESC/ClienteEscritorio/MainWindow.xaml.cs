using System.Windows;
using ec.edu.monster.Cliente.Servicios;
using Microsoft.Extensions.DependencyInjection;

namespace ClienteEscritorio
{
    /// <summary>
    /// Ventana del cliente de escritorio. Aloja la misma interfaz Blazor (SistemaConversorUI.Shared)
    /// que usan el cliente web y el movil, para mantener un unico diseno (RNF-006).
    /// URL del servicio SOAP: variable CONUNI_URL o http://localhost:5080/
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            var servicios = new ServiceCollection();
            servicios.AddWpfBlazorWebView();
            servicios.AgregarClienteMonster();
            blazorWebView.Services = servicios.BuildServiceProvider();
        }
    }
}
