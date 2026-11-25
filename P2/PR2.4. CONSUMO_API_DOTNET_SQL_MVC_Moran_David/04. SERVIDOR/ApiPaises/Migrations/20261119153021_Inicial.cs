using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ApiPaises.Migrations
{
    /// <inheritdoc />
    public partial class Inicial : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "pais",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Codigo = table.Column<string>(type: "char(3)", unicode: false, fixedLength: true, maxLength: 3, nullable: false),
                    Nombre = table.Column<string>(type: "nvarchar(120)", maxLength: 120, nullable: false),
                    Capital = table.Column<string>(type: "nvarchar(120)", maxLength: 120, nullable: false),
                    Region = table.Column<string>(type: "nvarchar(40)", maxLength: 40, nullable: false),
                    Subregion = table.Column<string>(type: "nvarchar(80)", maxLength: 80, nullable: false),
                    Poblacion = table.Column<long>(type: "bigint", nullable: false),
                    Area = table.Column<decimal>(type: "decimal(14,2)", precision: 14, scale: 2, nullable: false),
                    Moneda = table.Column<string>(type: "nvarchar(120)", maxLength: 120, nullable: false),
                    Bandera = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_pais", x => x.Id);
                    table.CheckConstraint("CK_pais_poblacion", "[Poblacion] >= 0");
                });

            migrationBuilder.CreateTable(
                name: "universidad",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Nombre = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: false),
                    Dominio = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: false),
                    SitioWeb = table.Column<string>(type: "nvarchar(300)", maxLength: 300, nullable: false),
                    Pais = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: false),
                    CodigoPais = table.Column<string>(type: "nvarchar(5)", maxLength: 5, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_universidad", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "IX_pais_Codigo",
                table: "pais",
                column: "Codigo",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_pais_Nombre",
                table: "pais",
                column: "Nombre");

            migrationBuilder.CreateIndex(
                name: "IX_pais_Region",
                table: "pais",
                column: "Region");

            migrationBuilder.CreateIndex(
                name: "IX_universidad_Nombre_Pais",
                table: "universidad",
                columns: new[] { "Nombre", "Pais" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "pais");

            migrationBuilder.DropTable(
                name: "universidad");
        }
    }
}
