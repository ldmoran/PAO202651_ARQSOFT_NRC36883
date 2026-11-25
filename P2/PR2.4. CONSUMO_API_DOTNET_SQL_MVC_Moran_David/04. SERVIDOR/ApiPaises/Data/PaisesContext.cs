using ApiPaises.Models;
using Microsoft.EntityFrameworkCore;

namespace ApiPaises.Data;

/// <summary>Contexto de Entity Framework: une las clases del modelo con las tablas de SQL Server.</summary>
public class PaisesContext : DbContext
{
    public PaisesContext(DbContextOptions<PaisesContext> opciones) : base(opciones) { }

    public DbSet<Pais> Paises => Set<Pais>();
    public DbSet<Universidad> Universidades => Set<Universidad>();

    protected override void OnModelCreating(ModelBuilder m)
    {
        m.Entity<Pais>(e =>
        {
            e.ToTable("pais", t => t.HasCheckConstraint("CK_pais_poblacion", "[Poblacion] >= 0"));
            e.Property(x => x.Codigo).HasMaxLength(3).IsFixedLength().IsUnicode(false).IsRequired();
            e.HasIndex(x => x.Codigo).IsUnique();
            e.Property(x => x.Nombre).HasMaxLength(120).IsRequired();
            e.Property(x => x.Capital).HasMaxLength(120);
            e.Property(x => x.Region).HasMaxLength(40).IsRequired();
            e.HasIndex(x => x.Region);
            e.HasIndex(x => x.Nombre);
            e.Property(x => x.Subregion).HasMaxLength(80);
            e.Property(x => x.Area).HasPrecision(14, 2);
            e.Property(x => x.Moneda).HasMaxLength(120);
            e.Property(x => x.Bandera).HasMaxLength(250);
        });
        m.Entity<Universidad>(e =>
        {
            e.ToTable("universidad");
            e.Property(x => x.Nombre).HasMaxLength(200).IsRequired();
            e.Property(x => x.Dominio).HasMaxLength(200);
            e.Property(x => x.SitioWeb).HasMaxLength(300);
            e.Property(x => x.Pais).HasMaxLength(100).IsRequired();
            e.Property(x => x.CodigoPais).HasMaxLength(5);
            e.HasIndex(x => new { x.Nombre, x.Pais }).IsUnique();
        });
    }
}
