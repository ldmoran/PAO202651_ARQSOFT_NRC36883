-- Ejecutar UNA vez como administrador de MySQL (root):
--   mysql -u root -p < 01_CrearUsuarioApp.sql
-- Crea la base y el usuario que usa el servidor SOAP. Cambia la clave si quieres; luego usa la misma
-- en CONUNI_DB_CLAVE (o en -Dconuni.db.clave=...) al ejecutar el servidor.
CREATE DATABASE IF NOT EXISTS conversion_unidades CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'conuni_app'@'localhost' IDENTIFIED BY 'ConUni2026!';
GRANT SELECT, INSERT, UPDATE ON conversion_unidades.* TO 'conuni_app'@'localhost';
FLUSH PRIVILEGES;
