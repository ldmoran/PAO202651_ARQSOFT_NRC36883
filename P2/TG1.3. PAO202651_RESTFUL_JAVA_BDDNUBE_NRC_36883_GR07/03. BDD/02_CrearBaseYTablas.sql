-- EurekaBank (MySQL 8): esquema normalizado. Ejecutar (despues de 01_CrearUsuarioApp.sql):
--   mysql -u root -p < 02_CrearBaseYTablas.sql
CREATE DATABASE IF NOT EXISTS eurekabank CHARACTER SET utf8mb4;
USE eurekabank;

DROP TABLE IF EXISTS movimiento;
DROP TABLE IF EXISTS cuenta;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS tipo_movimiento;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS sucursal;
DROP TABLE IF EXISTS moneda;
DROP TABLE IF EXISTS intento_acceso;

CREATE TABLE moneda (
    cod_moneda CHAR(2) NOT NULL PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sucursal (
    cod_sucursal CHAR(3) NOT NULL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    ciudad VARCHAR(30) NOT NULL,
    direccion VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE empleado (
    cod_empleado CHAR(4) NOT NULL PRIMARY KEY,
    apellido_paterno VARCHAR(25) NOT NULL,
    apellido_materno VARCHAR(25) NOT NULL DEFAULT '',
    nombre VARCHAR(30) NOT NULL,
    ciudad VARCHAR(30) NULL,
    direccion VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario (
    usuario VARCHAR(30) NOT NULL PRIMARY KEY,
    cod_empleado CHAR(4) NOT NULL,
    sal CHAR(16) NOT NULL,
    clave_hash CHAR(64) NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (cod_empleado) REFERENCES empleado (cod_empleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cliente (
    cod_cliente CHAR(5) NOT NULL PRIMARY KEY,
    apellido_paterno VARCHAR(25) NOT NULL,
    apellido_materno VARCHAR(25) NOT NULL,
    nombre VARCHAR(30) NOT NULL,
    cedula CHAR(8) NOT NULL UNIQUE,
    ciudad VARCHAR(30) NULL,
    direccion VARCHAR(50) NULL,
    telefono VARCHAR(20) NULL,
    email VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tipo_movimiento (
    cod_tipo CHAR(3) NOT NULL PRIMARY KEY,
    descripcion VARCHAR(40) NOT NULL,
    accion VARCHAR(10) NOT NULL,
    CHECK (accion IN ('INGRESO', 'SALIDA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cuenta (
    cod_cuenta CHAR(8) NOT NULL PRIMARY KEY,
    cod_moneda CHAR(2) NOT NULL,
    cod_sucursal CHAR(3) NOT NULL,
    cod_empleado_crea CHAR(4) NOT NULL,
    cod_cliente CHAR(5) NOT NULL,
    saldo DECIMAL(12,2) NOT NULL,
    fecha_creacion DATE NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',
    contador_mov INT NOT NULL DEFAULT 0,
    CHECK (saldo >= 0),
    FOREIGN KEY (cod_moneda) REFERENCES moneda (cod_moneda),
    FOREIGN KEY (cod_sucursal) REFERENCES sucursal (cod_sucursal),
    FOREIGN KEY (cod_empleado_crea) REFERENCES empleado (cod_empleado),
    FOREIGN KEY (cod_cliente) REFERENCES cliente (cod_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE movimiento (
    cod_cuenta CHAR(8) NOT NULL,
    numero INT NOT NULL,
    fecha DATE NOT NULL,
    cod_empleado CHAR(4) NOT NULL,
    cod_tipo CHAR(3) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    saldo_resultante DECIMAL(12,2) NOT NULL,
    cuenta_referencia CHAR(8) NULL,
    PRIMARY KEY (cod_cuenta, numero),
    CHECK (importe > 0),
    FOREIGN KEY (cod_cuenta) REFERENCES cuenta (cod_cuenta),
    FOREIGN KEY (cod_empleado) REFERENCES empleado (cod_empleado),
    FOREIGN KEY (cod_tipo) REFERENCES tipo_movimiento (cod_tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE intento_acceso (
    cod_intento INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(30) NOT NULL,
    exitoso TINYINT(1) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
