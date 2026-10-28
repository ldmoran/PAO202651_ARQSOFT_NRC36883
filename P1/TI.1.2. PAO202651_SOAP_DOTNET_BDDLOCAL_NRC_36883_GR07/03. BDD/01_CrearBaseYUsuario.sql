-- Ejecutar UNA vez con una cuenta administradora de SQL Server (por ejemplo con autenticacion de Windows):
--   sqlcmd -S localhost -E -C -i 01_CrearBaseYUsuario.sql
-- Crea la base y el inicio de sesion que usa el servidor SOAP, solo con permiso de leer y escribir datos.
-- Cambia la clave si quieres; luego usa la misma en ConnectionStrings:ConversionUnidades.
USE master;
GO
IF DB_ID('conversion_unidades') IS NULL
    CREATE DATABASE conversion_unidades;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'conuni_app')
    CREATE LOGIN conuni_app WITH PASSWORD = 'ConUni2026!', CHECK_POLICY = OFF;
GO
USE conversion_unidades;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'conuni_app')
    CREATE USER conuni_app FOR LOGIN conuni_app;
GO
ALTER ROLE db_datareader ADD MEMBER conuni_app;
ALTER ROLE db_datawriter ADD MEMBER conuni_app;
GO
