-- Ejecutar UNA vez como administrador de MySQL (root):  mysql -u root -p < 01_CrearBaseYUsuario.sql
-- Crea la base del taller y el usuario que usa el servidor. Cambia la clave si quieres (debe coincidir con PAISES_DB_CLAVE).
CREATE DATABASE IF NOT EXISTS api_paises CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'paises_app'@'localhost' IDENTIFIED BY 'Paises2026!';
GRANT ALL PRIVILEGES ON api_paises.* TO 'paises_app'@'localhost';
FLUSH PRIVILEGES;
