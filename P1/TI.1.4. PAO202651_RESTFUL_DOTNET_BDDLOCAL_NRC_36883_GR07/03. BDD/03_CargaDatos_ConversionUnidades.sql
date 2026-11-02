-- Datos iniciales: el usuario del sistema y el catalogo de categorias y unidades.
-- Ejecutar: sqlcmd -S localhost -E -C -i 03_CargaDatos_ConversionUnidades.sql
USE conversion_unidades;
GO

-- Usuario monster / clave monster9 (se guarda SHA-256(sal + clave), nunca la clave).
INSERT INTO dbo.usuario (nombre_usuario, sal_clave, hash_clave) VALUES
('monster', 'a3f1c9d27b4e5086d1f2a9b7c3e4d5f6', '4b6aa92fc59932c627242729eb132f7c3c6343e1aa6d907c00fda4dee58d3b30');

INSERT INTO dbo.categoria (nombre, descripcion) VALUES
('Longitud',    'Distancias: base metro'),
('Masa',        'Peso: base kilogramo'),
('Temperatura', 'Escalas termicas: base celsius');

-- Longitud (base: metro)
INSERT INTO dbo.unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden)
SELECT c.id_categoria, v.nombre, v.simbolo, v.factor, 0, v.orden
FROM dbo.categoria c
CROSS JOIN (VALUES ('centimetros', 'cm', 0.01, 1), ('metros', 'm', 1, 2), ('pies', 'ft', 0.3048, 3),
                   ('yardas', 'yd', 0.9144, 4), ('millas', 'mi', 1609.344, 5)) AS v (nombre, simbolo, factor, orden)
WHERE c.nombre = 'Longitud';

-- Masa (base: kilogramo; el quintal equivale a 100 libras)
INSERT INTO dbo.unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden)
SELECT c.id_categoria, v.nombre, v.simbolo, v.factor, 0, v.orden
FROM dbo.categoria c
CROSS JOIN (VALUES ('gramos', 'g', 0.001, 1), ('kilogramos', 'kg', 1, 2), ('onzas', 'oz', 0.028349523125, 3),
                   ('libras', 'lb', 0.45359237, 4), ('quintales', 'qq', 45.359237, 5)) AS v (nombre, simbolo, factor, orden)
WHERE c.nombre = 'Masa';

-- Temperatura (base: celsius). base = (valor + desplazamiento) x factor
--   fahrenheit: (F - 32) x 5/9     kelvin: K - 273,15     newton: N x 100/33     reaumur: Re x 5/4
INSERT INTO dbo.unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden)
SELECT c.id_categoria, v.nombre, v.simbolo, v.factor, v.desplazamiento, v.orden
FROM dbo.categoria c
CROSS JOIN (VALUES ('celsius', 'C', 1, 0, 1), ('fahrenheit', 'F', 0.555555555555556, -32, 2),
                   ('kelvin', 'K', 1, -273.15, 3), ('newton', 'N', 3.030303030303030, 0, 4),
                   ('reaumur', 'Re', 1.25, 0, 5)) AS v (nombre, simbolo, factor, desplazamiento, orden)
WHERE c.nombre = 'Temperatura';
GO
