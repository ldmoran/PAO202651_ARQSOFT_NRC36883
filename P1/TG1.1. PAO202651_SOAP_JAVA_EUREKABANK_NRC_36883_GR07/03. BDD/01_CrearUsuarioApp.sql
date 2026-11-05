-- Ejecutar UNA vez como administrador de MySQL (root):  mysql -u root -p < 01_CrearUsuarioApp.sql
-- Crea la base y el usuario que usa el servicio. Cambia la clave si quieres (debe coincidir con EUREKA_DB_CLAVE).
CREATE DATABASE IF NOT EXISTS eurekabank CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'eureka_app'@'localhost' IDENTIFIED BY 'Eureka2026!';
GRANT ALL PRIVILEGES ON eurekabank.* TO 'eureka_app'@'localhost';
FLUSH PRIVILEGES;
