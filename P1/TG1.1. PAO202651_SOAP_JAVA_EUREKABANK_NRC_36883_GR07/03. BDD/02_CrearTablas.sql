-- EurekaBank (MySQL 8): esquema normalizado. Ejecutar con: mysql -u eureka_app -p < 02_CrearTablas.sql
USE eurekabank;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS movimiento, cuenta, cliente, tipo_movimiento, usuario, empleado, sucursal, moneda, intento_acceso;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE moneda (
    cod_moneda  CHAR(2)      NOT NULL PRIMARY KEY,
    descripcion VARCHAR(30)  NOT NULL
);
CREATE TABLE sucursal (
    cod_sucursal CHAR(3)      NOT NULL PRIMARY KEY,
    nombre       VARCHAR(50)  NOT NULL,
    ciudad       VARCHAR(30)  NOT NULL,
    direccion    VARCHAR(50)
);
CREATE TABLE empleado (
    cod_empleado     CHAR(4)      NOT NULL PRIMARY KEY,
    apellido_paterno VARCHAR(25)  NOT NULL,
    apellido_materno VARCHAR(25)  NOT NULL DEFAULT '',
    nombre           VARCHAR(30)  NOT NULL,
    ciudad           VARCHAR(30),
    direccion        VARCHAR(50)
);
CREATE TABLE usuario (
    usuario      VARCHAR(30) NOT NULL PRIMARY KEY,
    cod_empleado CHAR(4)     NOT NULL,
    sal          CHAR(16)    NOT NULL,
    clave_hash   CHAR(64)    NOT NULL,
    estado       VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (cod_empleado) REFERENCES empleado (cod_empleado)
);
CREATE TABLE cliente (
    cod_cliente      CHAR(5)      NOT NULL PRIMARY KEY,
    apellido_paterno VARCHAR(25)  NOT NULL,
    apellido_materno VARCHAR(25)  NOT NULL,
    nombre           VARCHAR(30)  NOT NULL,
    cedula           CHAR(8)      NOT NULL UNIQUE,
    ciudad           VARCHAR(30),
    direccion        VARCHAR(50),
    telefono         VARCHAR(20),
    email            VARCHAR(50)
);
CREATE TABLE tipo_movimiento (
    cod_tipo    CHAR(3)     NOT NULL PRIMARY KEY,
    descripcion VARCHAR(40) NOT NULL,
    accion      VARCHAR(10) NOT NULL,
    CONSTRAINT ck_tipo_accion CHECK (accion IN ('INGRESO','SALIDA'))
);
CREATE TABLE cuenta (
    cod_cuenta        CHAR(8)       NOT NULL PRIMARY KEY,
    cod_moneda        CHAR(2)       NOT NULL,
    cod_sucursal      CHAR(3)       NOT NULL,
    cod_empleado_crea CHAR(4)       NOT NULL,
    cod_cliente       CHAR(5)       NOT NULL,
    saldo             DECIMAL(12,2) NOT NULL,
    fecha_creacion    DATE          NOT NULL,
    estado            VARCHAR(15)   NOT NULL DEFAULT 'ACTIVO',
    contador_mov      INT           NOT NULL DEFAULT 0,
    CONSTRAINT ck_cuenta_saldo CHECK (saldo >= 0),
    CONSTRAINT fk_cuenta_moneda   FOREIGN KEY (cod_moneda)        REFERENCES moneda (cod_moneda),
    CONSTRAINT fk_cuenta_sucursal FOREIGN KEY (cod_sucursal)      REFERENCES sucursal (cod_sucursal),
    CONSTRAINT fk_cuenta_empleado FOREIGN KEY (cod_empleado_crea) REFERENCES empleado (cod_empleado),
    CONSTRAINT fk_cuenta_cliente  FOREIGN KEY (cod_cliente)       REFERENCES cliente (cod_cliente)
);
CREATE TABLE movimiento (
    cod_cuenta        CHAR(8)       NOT NULL,
    numero            INT           NOT NULL,
    fecha             DATE          NOT NULL,
    cod_empleado      CHAR(4)       NOT NULL,
    cod_tipo          CHAR(3)       NOT NULL,
    importe           DECIMAL(12,2) NOT NULL,
    saldo_resultante  DECIMAL(12,2) NOT NULL,
    cuenta_referencia CHAR(8),
    PRIMARY KEY (cod_cuenta, numero),
    CONSTRAINT ck_mov_importe CHECK (importe > 0),
    CONSTRAINT fk_mov_cuenta   FOREIGN KEY (cod_cuenta)   REFERENCES cuenta (cod_cuenta),
    CONSTRAINT fk_mov_empleado FOREIGN KEY (cod_empleado) REFERENCES empleado (cod_empleado),
    CONSTRAINT fk_mov_tipo     FOREIGN KEY (cod_tipo)     REFERENCES tipo_movimiento (cod_tipo)
);
CREATE TABLE intento_acceso (
    cod_intento INT AUTO_INCREMENT PRIMARY KEY,
    usuario     VARCHAR(30) NOT NULL,
    exitoso     TINYINT(1)  NOT NULL,
    fecha_hora  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
