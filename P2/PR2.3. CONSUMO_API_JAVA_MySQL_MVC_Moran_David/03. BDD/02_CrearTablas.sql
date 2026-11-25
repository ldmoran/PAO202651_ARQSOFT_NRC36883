-- Taller: consumo de una API publica de paises con MySQL y MVC.
-- Ejecutar con: mysql -u paises_app -p < 02_CrearTablas.sql
CREATE DATABASE IF NOT EXISTS api_paises CHARACTER SET utf8mb4;
USE api_paises;
DROP TABLE IF EXISTS pais;
CREATE TABLE pais (
    id         INT            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo     CHAR(3)        NOT NULL UNIQUE,          -- codigo ISO alfa-3 (ECU), llave natural para importar sin duplicar
    nombre     VARCHAR(120)   NOT NULL,
    capital    VARCHAR(120)   NOT NULL DEFAULT '',
    region     VARCHAR(40)    NOT NULL,                  -- America, Europa, Asia, Africa, Oceania, Antartida
    subregion  VARCHAR(80)    NOT NULL DEFAULT '',
    poblacion  BIGINT         NOT NULL DEFAULT 0 CHECK (poblacion >= 0),
    area       DECIMAL(14,2)  NOT NULL DEFAULT 0 CHECK (area >= 0),
    moneda     VARCHAR(120)   NOT NULL DEFAULT '',
    bandera    VARCHAR(250)   NOT NULL DEFAULT ''
);
CREATE INDEX ix_pais_region ON pais (region);
CREATE INDEX ix_pais_nombre ON pais (nombre);
