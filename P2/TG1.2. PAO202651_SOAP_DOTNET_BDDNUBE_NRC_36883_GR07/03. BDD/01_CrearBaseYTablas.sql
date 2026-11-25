-- EurekaBank (SQL Server): esquema normalizado. Ejecutar:  sqlcmd -S localhost -E -i 01_CrearBaseYTablas.sql
IF DB_ID('eurekabank') IS NULL CREATE DATABASE eurekabank;
GO
USE eurekabank;
GO
IF OBJECT_ID('movimiento') IS NOT NULL DROP TABLE movimiento;
IF OBJECT_ID('cuenta') IS NOT NULL DROP TABLE cuenta;
IF OBJECT_ID('cliente') IS NOT NULL DROP TABLE cliente;
IF OBJECT_ID('tipo_movimiento') IS NOT NULL DROP TABLE tipo_movimiento;
IF OBJECT_ID('usuario') IS NOT NULL DROP TABLE usuario;
IF OBJECT_ID('empleado') IS NOT NULL DROP TABLE empleado;
IF OBJECT_ID('sucursal') IS NOT NULL DROP TABLE sucursal;
IF OBJECT_ID('moneda') IS NOT NULL DROP TABLE moneda;
IF OBJECT_ID('intento_acceso') IS NOT NULL DROP TABLE intento_acceso;
GO
CREATE TABLE moneda (cod_moneda CHAR(2) NOT NULL PRIMARY KEY, descripcion NVARCHAR(30) NOT NULL);
CREATE TABLE sucursal (cod_sucursal CHAR(3) NOT NULL PRIMARY KEY, nombre NVARCHAR(50) NOT NULL, ciudad NVARCHAR(30) NOT NULL, direccion NVARCHAR(50) NULL);
CREATE TABLE empleado (cod_empleado CHAR(4) NOT NULL PRIMARY KEY, apellido_paterno NVARCHAR(25) NOT NULL, apellido_materno NVARCHAR(25) NOT NULL DEFAULT '', nombre NVARCHAR(30) NOT NULL, ciudad NVARCHAR(30) NULL, direccion NVARCHAR(50) NULL);
CREATE TABLE usuario (usuario NVARCHAR(30) NOT NULL PRIMARY KEY, cod_empleado CHAR(4) NOT NULL REFERENCES empleado(cod_empleado), sal CHAR(16) NOT NULL, clave_hash CHAR(64) NOT NULL, estado NVARCHAR(15) NOT NULL DEFAULT 'ACTIVO');
CREATE TABLE cliente (cod_cliente CHAR(5) NOT NULL PRIMARY KEY, apellido_paterno NVARCHAR(25) NOT NULL, apellido_materno NVARCHAR(25) NOT NULL, nombre NVARCHAR(30) NOT NULL, cedula CHAR(8) NOT NULL UNIQUE, ciudad NVARCHAR(30) NULL, direccion NVARCHAR(50) NULL, telefono NVARCHAR(20) NULL, email NVARCHAR(50) NULL);
CREATE TABLE tipo_movimiento (cod_tipo CHAR(3) NOT NULL PRIMARY KEY, descripcion NVARCHAR(40) NOT NULL, accion NVARCHAR(10) NOT NULL CHECK (accion IN ('INGRESO','SALIDA')));
CREATE TABLE cuenta (cod_cuenta CHAR(8) NOT NULL PRIMARY KEY, cod_moneda CHAR(2) NOT NULL REFERENCES moneda(cod_moneda), cod_sucursal CHAR(3) NOT NULL REFERENCES sucursal(cod_sucursal), cod_empleado_crea CHAR(4) NOT NULL REFERENCES empleado(cod_empleado), cod_cliente CHAR(5) NOT NULL REFERENCES cliente(cod_cliente), saldo DECIMAL(12,2) NOT NULL CHECK (saldo >= 0), fecha_creacion DATE NOT NULL, estado NVARCHAR(15) NOT NULL DEFAULT 'ACTIVO', contador_mov INT NOT NULL DEFAULT 0);
CREATE TABLE movimiento (cod_cuenta CHAR(8) NOT NULL REFERENCES cuenta(cod_cuenta), numero INT NOT NULL, fecha DATE NOT NULL, cod_empleado CHAR(4) NOT NULL REFERENCES empleado(cod_empleado), cod_tipo CHAR(3) NOT NULL REFERENCES tipo_movimiento(cod_tipo), importe DECIMAL(12,2) NOT NULL CHECK (importe > 0), saldo_resultante DECIMAL(12,2) NOT NULL, cuenta_referencia CHAR(8) NULL, PRIMARY KEY (cod_cuenta, numero));
CREATE TABLE intento_acceso (cod_intento INT IDENTITY PRIMARY KEY, usuario NVARCHAR(30) NOT NULL, exitoso BIT NOT NULL, fecha_hora DATETIME NOT NULL DEFAULT GETDATE());
GO
