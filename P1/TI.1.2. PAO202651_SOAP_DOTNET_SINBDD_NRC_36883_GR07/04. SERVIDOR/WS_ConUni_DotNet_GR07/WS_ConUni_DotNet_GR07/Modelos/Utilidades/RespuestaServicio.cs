using System.Runtime.Serialization;
using ec.edu.monster.Modelos.Utilidades.Enums;

namespace ec.edu.monster.Modelos.Utilidades
{
    /// <summary>
    /// Respuesta estructurada que devuelven todas las operaciones del servicio
    /// (CU-07). Se serializa a XML dentro del cuerpo del mensaje SOAP y se arma
    /// con <see cref="Mapeadores.RespuestaMapeador"/>.
    ///
    /// estado: OK o ERROR.
    /// codigo: OK-200, AUTH-001, AUTH-002, AUTH-003, UNIT-001, DATA-001, SRV-500.
    /// mensaje: texto informativo para el usuario.
    /// resultado: valor convertido (nulo si hay error o si la operacion no calcula).
    /// unidadOrigen / unidadDestino: unidades de la solicitud (conversiones).
    /// token: token generado (solo en el login).
    /// datos: listas de texto (categorias o unidades del catalogo).
    /// </summary>
    [DataContract(Name = "respuestaServicio", Namespace = "http://WebServices.monster.edu.ec/")]
    public class RespuestaServicio
    {
        [DataMember(Name = "estado", Order = 1)]
        public string? Estado { get; set; }

        [DataMember(Name = "codigo", Order = 2)]
        public string? Codigo { get; set; }

        [DataMember(Name = "mensaje", Order = 3)]
        public string? Mensaje { get; set; }

        [DataMember(Name = "resultado", Order = 4, EmitDefaultValue = false)]
        public double? Resultado { get; set; }

        [DataMember(Name = "unidadOrigen", Order = 5, EmitDefaultValue = false)]
        public string? UnidadOrigen { get; set; }

        [DataMember(Name = "unidadDestino", Order = 6, EmitDefaultValue = false)]
        public string? UnidadDestino { get; set; }

        [DataMember(Name = "token", Order = 7, EmitDefaultValue = false)]
        public string? Token { get; set; }

        [DataMember(Name = "datos", Order = 8)]
        public List<string> Datos { get; set; } = new();

        public bool EsExitosa() => EstadoRespuesta.Ok.Texto() == Estado;
    }
}
