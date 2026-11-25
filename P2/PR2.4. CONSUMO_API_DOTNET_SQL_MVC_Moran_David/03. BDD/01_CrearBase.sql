-- Crea la base del taller en SQL Server. Es opcional: la aplicacion tambien la crea sola al iniciar (migraciones de Entity Framework).
IF DB_ID(N'api_paises') IS NULL
    CREATE DATABASE api_paises;
GO
