-- Datos iniciales: el usuario del sistema y el catalogo de categorias y unidades.
USE conversion_unidades;

-- Usuario monster / clave monster9 (se guarda SHA-256(sal + clave), nunca la clave).
INSERT INTO usuario (nombre_usuario, sal_clave, hash_clave) VALUES
('monster', 'a3f1c9d27b4e5086d1f2a9b7c3e4d5f6', '4b6aa92fc59932c627242729eb132f7c3c6343e1aa6d907c00fda4dee58d3b30');

INSERT INTO categoria (nombre, descripcion) VALUES
('Longitud',    'Distancias: base metro'),
('Masa',        'Peso: base kilogramo'),
('Temperatura', 'Escalas termicas: base celsius');

-- Longitud (base: metro)
INSERT INTO unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden) VALUES
(1, 'centimetros', 'cm', 0.01,      0, 1),
(1, 'metros',      'm',  1,         0, 2),
(1, 'pies',        'ft', 0.3048,    0, 3),
(1, 'yardas',      'yd', 0.9144,    0, 4),
(1, 'millas',      'mi', 1609.344,  0, 5);

-- Masa (base: kilogramo; el quintal equivale a 100 libras)
INSERT INTO unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden) VALUES
(2, 'gramos',     'g',  0.001,          0, 1),
(2, 'kilogramos', 'kg', 1,              0, 2),
(2, 'onzas',      'oz', 0.028349523125, 0, 3),
(2, 'libras',     'lb', 0.45359237,     0, 4),
(2, 'quintales',  'qq', 45.359237,      0, 5);

-- Temperatura (base: celsius). base = (valor + desplazamiento) x factor
--   fahrenheit: (F - 32) x 5/9     kelvin: K - 273,15     newton: N x 100/33     reaumur: Re x 5/4
INSERT INTO unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden) VALUES
(3, 'celsius',    'C',  1,                  0,       1),
(3, 'fahrenheit', 'F',  0.555555555555556, -32,      2),
(3, 'kelvin',     'K',  1,                 -273.15,  3),
(3, 'newton',     'N',  3.030303030303030,  0,       4),
(3, 'reaumur',    'Re', 1.25,               0,       5);
