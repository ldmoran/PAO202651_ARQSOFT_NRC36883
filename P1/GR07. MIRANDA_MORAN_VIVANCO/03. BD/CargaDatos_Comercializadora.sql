-- ============================================================
-- COMERCIALIZADORA - catalogo inicial de telefonos
-- ============================================================
USE comercializadora;

DELETE FROM FACTURA;
DELETE FROM TELEFONO;
DELETE FROM CLIENTE;
ALTER TABLE CLIENTE AUTO_INCREMENT = 1;
ALTER TABLE TELEFONO AUTO_INCREMENT = 1;
ALTER TABLE FACTURA AUTO_INCREMENT = 1;

INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, DISPONIBLE) VALUES
('Samsung', 'Galaxy A15', 189.90, 1),
('Xiaomi', 'Redmi Note 13', 229.00, 1),
('Motorola', 'Moto G54', 199.99, 1),
('Honor', '90 Lite', 299.00, 1),
('Samsung', 'Galaxy S23 FE', 549.00, 1),
('Apple', 'iPhone 13', 649.00, 1),
('Google', 'Pixel 8', 699.00, 1),
('Apple', 'iPhone 15', 899.00, 1);
