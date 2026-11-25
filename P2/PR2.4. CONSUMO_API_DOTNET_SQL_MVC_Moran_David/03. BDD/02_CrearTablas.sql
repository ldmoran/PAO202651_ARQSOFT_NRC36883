IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;
GO

BEGIN TRANSACTION;
IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE TABLE [pais] (
        [Id] int NOT NULL IDENTITY,
        [Codigo] char(3) NOT NULL,
        [Nombre] nvarchar(120) NOT NULL,
        [Capital] nvarchar(120) NOT NULL,
        [Region] nvarchar(40) NOT NULL,
        [Subregion] nvarchar(80) NOT NULL,
        [Poblacion] bigint NOT NULL,
        [Area] decimal(14,2) NOT NULL,
        [Moneda] nvarchar(120) NOT NULL,
        [Bandera] nvarchar(250) NOT NULL,
        CONSTRAINT [PK_pais] PRIMARY KEY ([Id]),
        CONSTRAINT [CK_pais_poblacion] CHECK ([Poblacion] >= 0)
    );
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE TABLE [universidad] (
        [Id] int NOT NULL IDENTITY,
        [Nombre] nvarchar(200) NOT NULL,
        [Dominio] nvarchar(200) NOT NULL,
        [SitioWeb] nvarchar(300) NOT NULL,
        [Pais] nvarchar(100) NOT NULL,
        [CodigoPais] nvarchar(5) NOT NULL,
        CONSTRAINT [PK_universidad] PRIMARY KEY ([Id])
    );
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE UNIQUE INDEX [IX_pais_Codigo] ON [pais] ([Codigo]);
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE INDEX [IX_pais_Nombre] ON [pais] ([Nombre]);
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE INDEX [IX_pais_Region] ON [pais] ([Region]);
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    CREATE UNIQUE INDEX [IX_universidad_Nombre_Pais] ON [universidad] ([Nombre], [Pais]);
END;

IF NOT EXISTS (
    SELECT * FROM [__EFMigrationsHistory]
    WHERE [MigrationId] = N'20261119153021_Inicial'
)
BEGIN
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20261119153021_Inicial', N'10.0.15');
END;

COMMIT;
GO

