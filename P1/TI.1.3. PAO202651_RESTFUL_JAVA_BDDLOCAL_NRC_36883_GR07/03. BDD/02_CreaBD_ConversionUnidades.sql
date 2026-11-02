-- ============================================================
-- CONVERSION DE UNIDADES MONSTER - Base de datos local
-- Motor: MySQL 8      Esquema: conversion_unidades
-- Tablas: USUARIO, CATEGORIA, UNIDAD, SESION, INTENTO_ACCESO, CONVERSION
-- ============================================================
CREATE DATABASE IF NOT EXISTS conversion_unidades CHARACTER SET utf8mb4;
USE conversion_unidades;

DROP VIEW  IF EXISTS v_historial_conversiones;
DROP TABLE IF EXISTS conversion;
DROP TABLE IF EXISTS intento_acceso;
DROP TABLE IF EXISTS sesion;
DROP TABLE IF EXISTS unidad;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;

-- Usuarios del sistema. La clave nunca se guarda: se guarda SHA-256(sal + clave) en hexadecimal.
CREATE TABLE usuario (
    id_usuario     INT          NOT NULL AUTO_INCREMENT,
    nombre_usuario VARCHAR(50)  NOT NULL,
    sal_clave      CHAR(32)     NOT NULL,
    hash_clave     CHAR(64)     NOT NULL,
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario)
);

-- Categorias de unidades (Longitud, Masa, Temperatura...). Agregar una fila agrega una categoria.
CREATE TABLE categoria (
    id_categoria INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(200) NULL,
    activa       TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

-- Unidades de cada categoria. Se convierte con: base = (valor + desplazamiento) x factor_base,
-- y desde la base: valor = base / factor_base - desplazamiento. En longitud y masa el
-- desplazamiento es 0; en temperatura la base es el celsius.
CREATE TABLE unidad (
    id_unidad      INT            NOT NULL AUTO_INCREMENT,
    id_categoria   INT            NOT NULL,
    nombre         VARCHAR(50)    NOT NULL,           -- en minusculas y sin tildes: "centimetros"
    simbolo        VARCHAR(10)    NOT NULL,
    factor_base    DECIMAL(30,15) NOT NULL,
    desplazamiento DECIMAL(30,15) NOT NULL DEFAULT 0,
    orden          INT            NOT NULL,           -- orden en que se muestra dentro de la categoria
    activa         TINYINT(1)     NOT NULL DEFAULT 1,
    CONSTRAINT pk_unidad PRIMARY KEY (id_unidad),
    CONSTRAINT uq_unidad_nombre UNIQUE (nombre),
    CONSTRAINT fk_unidad_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria),
    CONSTRAINT ck_unidad_factor CHECK (factor_base > 0)
);

-- Sesiones (tokens) emitidas al iniciar sesion. Un token vale 30 minutos por defecto.
CREATE TABLE sesion (
    id_sesion        BIGINT      NOT NULL AUTO_INCREMENT,
    token            CHAR(36)    NOT NULL,
    id_usuario       INT         NOT NULL,
    fecha_emision    DATETIME(3) NOT NULL,
    fecha_expiracion DATETIME(3) NOT NULL,
    activa           TINYINT(1)  NOT NULL DEFAULT 1,
    CONSTRAINT pk_sesion PRIMARY KEY (id_sesion),
    CONSTRAINT uq_sesion_token UNIQUE (token),
    CONSTRAINT fk_sesion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_sesion_fechas CHECK (fecha_expiracion > fecha_emision)
);

-- Bitacora de intentos de inicio de sesion (exitosos y fallidos).
CREATE TABLE intento_acceso (
    id_intento       BIGINT       NOT NULL AUTO_INCREMENT,
    id_usuario       INT          NULL,                -- nulo si el usuario escrito no existe
    nombre_ingresado VARCHAR(50)  NOT NULL,
    exitoso          TINYINT(1)   NOT NULL,
    detalle          VARCHAR(100) NOT NULL,
    fecha_hora       DATETIME(3)  NOT NULL,
    CONSTRAINT pk_intento_acceso PRIMARY KEY (id_intento),
    CONSTRAINT fk_intento_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- Historial de conversiones realizadas, ligado a la sesion que las pidio.
CREATE TABLE conversion (
    id_conversion     BIGINT      NOT NULL AUTO_INCREMENT,
    id_sesion         BIGINT      NOT NULL,
    id_unidad_origen  INT         NOT NULL,
    id_unidad_destino INT         NOT NULL,
    valor_entrada     DOUBLE      NOT NULL,
    resultado         DOUBLE      NOT NULL,
    fecha_hora        DATETIME(3) NOT NULL,
    CONSTRAINT pk_conversion PRIMARY KEY (id_conversion),
    CONSTRAINT fk_conversion_sesion FOREIGN KEY (id_sesion) REFERENCES sesion (id_sesion),
    CONSTRAINT fk_conversion_origen FOREIGN KEY (id_unidad_origen) REFERENCES unidad (id_unidad),
    CONSTRAINT fk_conversion_destino FOREIGN KEY (id_unidad_destino) REFERENCES unidad (id_unidad)
);

CREATE INDEX ix_sesion_usuario ON sesion (id_usuario);
CREATE INDEX ix_conversion_sesion ON conversion (id_sesion);
CREATE INDEX ix_conversion_fecha ON conversion (fecha_hora);
CREATE INDEX ix_intento_fecha ON intento_acceso (fecha_hora);

-- Consulta lista para reportes: quien convirtio que, y cuando.
CREATE VIEW v_historial_conversiones AS
SELECT c.id_conversion, c.fecha_hora, u.nombre_usuario, g.nombre AS categoria,
       uo.nombre AS unidad_origen, c.valor_entrada, ud.nombre AS unidad_destino, c.resultado
FROM conversion c
JOIN sesion s    ON s.id_sesion = c.id_sesion
JOIN usuario u   ON u.id_usuario = s.id_usuario
JOIN unidad uo   ON uo.id_unidad = c.id_unidad_origen
JOIN unidad ud   ON ud.id_unidad = c.id_unidad_destino
JOIN categoria g ON g.id_categoria = uo.id_categoria;
