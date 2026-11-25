EurekaBank - base de datos local MySQL 8 (esquema eurekabank)
GRUPO 7: Miranda Alison, Morán Lenin, Vivanco Gabriel

Es la misma base de TG1.2 (SQL Server) pasada a MySQL: mismas 9 tablas y mismos datos. Ejecutar en este orden:

1) 01_CrearUsuarioApp.sql       (una sola vez, como root) crea la base y el usuario eureka_app
2) 02_CrearBaseYTablas.sql      crea las tablas (borra y vuelve a crear si ya existen)
3) 03_CargaDatos.sql            inserta empleados, usuarios, clientes, cuentas y movimientos de ejemplo

   mysql -u root -p < 01_CrearUsuarioApp.sql
   mysql -u root -p < 02_CrearBaseYTablas.sql
   mysql -u root -p < 03_CargaDatos.sql

Para dejar la base limpia otra vez, repite solo los pasos 2 y 3.

Al ejecutar el servidor se entrega la clave del usuario eureka_app con -Deureka.db.clave=... o con la variable de
entorno EUREKA_DB_CLAVE (por defecto en el script: EurekaDb2026!).

Usuarios de demostración (clave con sal + SHA-256): lmoran, amiranda, gvivanco · clave Eureka2026!
Cuentas de ejemplo: 00100001, 00100002, 00200001, 00200002, 00100003 (la 00100004 está ANULADA).
