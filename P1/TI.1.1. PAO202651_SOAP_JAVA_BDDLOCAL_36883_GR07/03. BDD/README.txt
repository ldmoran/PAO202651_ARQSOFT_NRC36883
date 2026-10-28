Proyecto de conversión de unidades
GRUPO 7:
Miranda Alison
Morán Lenin
Vivanco Gabriel

Scripts de la base de datos local MySQL 8 (esquema conversion_unidades). Ejecutar en este orden:

1) 01_CrearUsuarioApp.sql        (una sola vez, como root) crea la base y el usuario conuni_app
2) 02_CreaBD_ConversionUnidades.sql   crea las tablas, índices y la vista v_historial_conversiones
3) 03_CargaDatos_ConversionUnidades.sql   inserta el usuario monster y el catálogo de unidades

   mysql -u root -p < 01_CrearUsuarioApp.sql
   mysql -u root -p < 02_CreaBD_ConversionUnidades.sql
   mysql -u root -p < 03_CargaDatos_ConversionUnidades.sql

Luego, al ejecutar el servidor, se entrega la clave del usuario conuni_app con -Dconuni.db.clave=... o con la
variable de entorno CONUNI_DB_CLAVE. Las credenciales del sistema son:
Usuario: monster
Contraseña: monster9
