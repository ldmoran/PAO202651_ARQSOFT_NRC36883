Proyecto de conversión de unidades
GRUPO 7:
Miranda Alison
Morán Lenin
Vivanco Gabriel

Scripts de la base de datos local SQL Server (base conversion_unidades). Ejecutar en este orden:

1) 01_CrearBaseYUsuario.sql      (una sola vez, con una cuenta administradora) crea la base y el inicio de sesión conuni_app
2) 02_CreaBD_ConversionUnidades.sql   crea las tablas, índices y la vista v_historial_conversiones
3) 03_CargaDatos_ConversionUnidades.sql   inserta el usuario monster y el catálogo de unidades

   sqlcmd -S localhost -E -C -i 01_CrearBaseYUsuario.sql
   sqlcmd -S localhost -E -C -b -i 02_CreaBD_ConversionUnidades.sql
   sqlcmd -S localhost -E -C -b -i 03_CargaDatos_ConversionUnidades.sql

El servidor se conecta con la cadena ConnectionStrings:ConversionUnidades de appsettings.json (autenticación de Windows)
o con la variable de entorno CONUNI_DB_CONEXION. Las credenciales del sistema son:
Usuario: monster
Contraseña: monster9
