using ec.edu.monster.ClienteConsola.Controladores;

namespace ec.edu.monster.ClienteConsola.Vistas
{
    /// <summary>Pantallas del catalogo y de la conversion (CU-02, CU-03 y CU-04).</summary>
    public class VistaConversion
    {
        private readonly ControladorConversiones controlador;
        private readonly ConsolaIO io;

        public VistaConversion(ControladorConversiones controlador, ConsolaIO io)
        {
            this.controlador = controlador;
            this.io = io;
        }

        public async Task VerCatalogo()
        {
            var cats = await controlador.Categorias();
            if (!cats.EsExitosa)
            {
                io.Error(cats);
                return;
            }
            io.Seccion("Catálogo de unidades");
            foreach (var c in cats.Datos)
            {
                var u = await controlador.Unidades(c);
                if (!u.EsExitosa)
                {
                    io.Error(u);
                    return;
                }
                io.Linea($"  {c}: {string.Join(", ", u.Datos)}");
            }
        }

        public async Task Convertir()
        {
            var cats = await controlador.Categorias();
            if (!cats.EsExitosa)
            {
                io.Error(cats);
                return;
            }
            var categoria = io.Elegir("Categoría", cats.Datos);
            if (categoria == null)
            {
                return;
            }
            var un = await controlador.Unidades(categoria);
            if (!un.EsExitosa)
            {
                io.Error(un);
                return;
            }
            var origen = io.Elegir("Unidad de origen", un.Datos);
            if (origen == null)
            {
                return;
            }
            var destino = io.Elegir("Unidad de destino", un.Datos);
            if (destino == null)
            {
                return;
            }
            var valor = io.LeerNumero("Valor a convertir: ");
            if (valor == null)
            {
                return;
            }
            var r = await controlador.Convertir(categoria, valor.Value, origen, destino);
            if (r.EsExitosa && r.Resultado != null)
            {
                io.Linea();
                io.Marco("RESULTADO",
                    $"{ControladorConversiones.Formatear(valor.Value)} {r.UnidadOrigen}",
                    "=",
                    $"{ControladorConversiones.Formatear(r.Resultado.Value)} {r.UnidadDestino}");
                io.Linea("  " + r.Mensaje);
            }
            else
            {
                io.Error(r);
            }
        }
    }
}
