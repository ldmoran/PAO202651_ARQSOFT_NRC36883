-- Ejecutar UNA vez como administrador de MySQL (root). Crea el usuario que usan los servicios.
-- Cambia la clave si quieres; luego usa la misma en BANQUITO_DB_CLAVE y COMERCIALIZADORA_DB_CLAVE.
CREATE USER IF NOT EXISTS 'banquito_app'@'localhost' IDENTIFIED BY 'Banquito2026!';
CREATE DATABASE IF NOT EXISTS banquito CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS comercializadora CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON banquito.* TO 'banquito_app'@'localhost';
GRANT ALL PRIVILEGES ON comercializadora.* TO 'banquito_app'@'localhost';
FLUSH PRIVILEGES;
