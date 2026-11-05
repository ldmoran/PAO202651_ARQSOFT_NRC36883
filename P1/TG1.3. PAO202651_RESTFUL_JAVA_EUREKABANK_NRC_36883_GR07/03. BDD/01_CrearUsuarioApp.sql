-- Ejecutar UNA vez como administrador de MySQL (root):
--   mysql -u root -p < 01_CrearUsuarioApp.sql
-- Crea la base y el usuario que usa el servidor REST de EurekaBank. Cambia la clave si quieres; luego usa la misma
-- en EUREKA_DB_CLAVE (o en -Deureka.db.clave=...) al ejecutar el servidor.
CREATE DATABASE IF NOT EXISTS eurekabank CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'eureka_app'@'localhost' IDENTIFIED BY 'EurekaDb2026!';
GRANT SELECT, INSERT, UPDATE ON eurekabank.* TO 'eureka_app'@'localhost';
FLUSH PRIVILEGES;
