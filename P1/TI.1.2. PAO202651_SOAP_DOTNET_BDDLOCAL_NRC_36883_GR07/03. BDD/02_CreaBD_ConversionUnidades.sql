-- ============================================================
-- CONVERSION DE UNIDADES MONSTER - Base de datos local
-- Motor: SQL Server 2019 o superior      Base: conversion_unidades
-- Tablas: usuario, categoria, unidad, sesion, intento_acceso, conversion
-- Ejecutar: sqlcmd -S localhost -E -C -i 02_CreaBD_ConversionUnidades.sql
-- ============================================================
IF DB_ID('conversion_unidades') IS NULL
    CREATE DATABASE conversion_unidades;
GO
USE conversion_unidades;
GO

DROP VIEW  IF EXISTS dbo.v_historial_conversiones;
DROP TABLE IF EXISTS dbo.conversion;
DROP TABLE IF EXISTS dbo.intento_acceso;
DROP TABLE IF EXISTS dbo.sesion;
DROP TABLE IF EXISTS dbo.unidad;
DROP TABLE IF EXISTS dbo.categoria;
DROP TABLE IF EXISTS dbo.usuario;
GO

-- Usuarios del sistema. La clave nunca se guarda: se guarda SHA-256(sal + clave) en hexadecimal.
CREATE TABLE dbo.usuario (
    id_usuario     INT          IDENTITY(1,1) NOT NULL,
    nombre_usuario VARCHAR(50)  NOT NULL,
    sal_clave      CHAR(32)     NOT NULL,
    hash_clave     CHAR(64)     NOT NULL,
    activo         BIT          NOT NULL CONSTRAINT df_usuario_activo DEFAULT 1,
    fecha_creacion DATETIME2(3) NOT NULL CONSTRAINT df_usuario_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario)
);

-- Categorias de unidades (Longitud, Masa, Temperatura...). Agregar una fila agrega una categoria.
CREATE TABLE dbo.categoria (
    id_categoria INT          IDENTITY(1,1) NOT NULL,
    nombre       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(200) NULL,
    activa       BIT          NOT NULL CONSTRAINT df_categoria_activa DEFAULT 1,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

-- Unidades de cada categoria. Se convierte con: base = (valor + desplazamiento) x factor_base,
-- y desde la base: valor = base / factor_base - desplazamiento. En longitud y masa el
-- desplazamiento es 0; en temperatura la base es el celsius.
CREATE TABLE dbo.unidad (
    id_unidad      INT            IDENTITY(1,1) NOT NULL,
    id_categoria   INT            NOT NULL,
    nombre         VARCHAR(50)    NOT NULL,           -- en minusculas y sin tildes: "centimetros"
    simbolo        VARCHAR(10)    NOT NULL,
    factor_base    DECIMAL(30,15) NOT NULL,
    desplazamiento DECIMAL(30,15) NOT NULL CONSTRAINT df_unidad_desplazamiento DEFAULT 0,
    orden          INT            NOT NULL,           -- orden en que se muestra dentro de la categoria
    activa         BIT            NOT NULL CONSTRAINT df_unidad_activa DEFAULT 1,
    CONSTRAINT pk_unidad PRIMARY KEY (id_unidad),
    CONSTRAINT uq_unidad_nombre UNIQUE (nombre),
    CONSTRAINT fk_unidad_categoria FOREIGN KEY (id_categoria) REFERENCES dbo.categoria (id_categoria),
    CONSTRAINT ck_unidad_factor CHECK (factor_base > 0)
);

-- Sesiones (tokens) emitidas al iniciar sesion. Un token vale 30 minutos por defecto.
CREATE TABLE dbo.sesion (
    id_sesion        BIGINT       IDENTITY(1,1) NOT NULL,
    token            CHAR(36)     NOT NULL,
    id_usuario       INT          NOT NULL,
    fecha_emision    DATETIME2(3) NOT NULL,
    fecha_expiracion DATETIME2(3) NOT NULL,
    activa           BIT          NOT NULL CONSTRAINT df_sesion_activa DEFAULT 1,
    CONSTRAINT pk_sesion PRIMARY KEY (id_sesion),
    CONSTRAINT uq_sesion_token UNIQUE (token),
    CONSTRAINT fk_sesion_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario (id_usuario),
    CONSTRAINT ck_sesion_fechas CHECK (fecha_expiracion > fecha_emision)
);

-- Bitacora de intentos de inicio de sesion (exitosos y fallidos).
CREATE TABLE dbo.intento_acceso (
    id_intento       BIGINT       IDENTITY(1,1) NOT NULL,
    id_usuario       INT          NULL,                -- nulo si el usuario escrito no existe
    nombre_ingresado VARCHAR(50)  NOT NULL,
    exitoso          BIT          NOT NULL,
    detalle          VARCHAR(100) NOT NULL,
    fecha_hora       DATETIME2(3) NOT NULL,
    CONSTRAINT pk_intento_acceso PRIMARY KEY (id_intento),
    CONSTRAINT fk_intento_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario (id_usuario)
);

-- Historial de conversiones realizadas, ligado a la sesion que las pidio.
CREATE TABLE dbo.conversion (
    id_conversion     BIGINT       IDENTITY(1,1) NOT NULL,
    id_sesion         BIGINT       NOT NULL,
    id_unidad_origen  INT          NOT NULL,
    id_unidad_destino INT          NOT NULL,
    valor_entrada     FLOAT        NOT NULL,
    resultado         FLOAT        NOT NULL,
    fecha_hora        DATETIME2(3) NOT NULL,
    CONSTRAINT pk_conversion PRIMARY KEY (id_conversion),
    CONSTRAINT fk_conversion_sesion FOREIGN KEY (id_sesion) REFERENCES dbo.sesion (id_sesion),
    CONSTRAINT fk_conversion_origen FOREIGN KEY (id_unidad_origen) REFERENCES dbo.unidad (id_unidad),
    CONSTRAINT fk_conversion_destino FOREIGN KEY (id_unidad_destino) REFERENCES dbo.unidad (id_unidad)
);
GO

CREATE INDEX ix_sesion_usuario ON dbo.sesion (id_usuario);
CREATE INDEX ix_conversion_sesion ON dbo.conversion (id_sesion);
CREATE INDEX ix_conversion_fecha ON dbo.conversion (fecha_hora);
CREATE INDEX ix_intento_fecha ON dbo.intento_acceso (fecha_hora);
GO

-- Consulta lista para reportes: quien convirtio que, y cuando.
CREATE VIEW dbo.v_historial_conversiones AS
SELECT c.id_conversion, c.fecha_hora, u.nombre_usuario, g.nombre AS categoria,
       uo.nombre AS unidad_origen, c.valor_entrada, ud.nombre AS unidad_destino, c.resultado
FROM dbo.conversion c
JOIN dbo.sesion s    ON s.id_sesion = c.id_sesion
JOIN dbo.usuario u   ON u.id_usuario = s.id_usuario
JOIN dbo.unidad uo   ON uo.id_unidad = c.id_unidad_origen
JOIN dbo.unidad ud   ON ud.id_unidad = c.id_unidad_destino
JOIN dbo.categoria g ON g.id_categoria = uo.id_categoria;
GO
