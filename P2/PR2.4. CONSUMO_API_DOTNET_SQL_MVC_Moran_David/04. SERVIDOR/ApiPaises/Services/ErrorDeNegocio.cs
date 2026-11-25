namespace ApiPaises.Services;

/// <summary>Un error que se le puede mostrar tal cual al usuario (dato invalido, no existe, etc.).</summary>
public class ErrorDeNegocio : Exception
{
    public int Codigo { get; }

    public ErrorDeNegocio(string mensaje, int codigo = 400) : base(mensaje)
    {
        Codigo = codigo;
    }
}
