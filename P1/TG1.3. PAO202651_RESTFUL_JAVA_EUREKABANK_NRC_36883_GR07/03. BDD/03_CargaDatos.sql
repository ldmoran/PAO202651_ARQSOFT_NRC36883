USE eurekabank;
SET NAMES utf8mb4;
-- Datos iniciales de EurekaBank (MySQL; usuarios con sal + SHA-256; clave de demostracion: Eureka2026!)
INSERT INTO moneda (cod_moneda, descripcion) VALUES ('01','Soles'),('02','Dólares');
INSERT INTO sucursal (cod_sucursal, nombre, ciudad, direccion) VALUES ('001','Quito Centro','Quito','Av. Amazonas N24-60'),('002','Sangolquí','Sangolquí','Av. General Enríquez 123');
INSERT INTO empleado (cod_empleado, apellido_paterno, apellido_materno, nombre, ciudad, direccion) VALUES
('0001','Morán','','Lenin','Quito','Quito'),('0002','Miranda','','Alison','Quito','Quito'),('0003','Vivanco','','Gabriel','Quito','Quito');
INSERT INTO usuario (usuario, cod_empleado, sal, clave_hash, estado) VALUES
('lmoran','0001','a1a6e7cbcfd7e9f1','86a357fd59c6b18cbe85d78f5a5df101258900952f576d7bf82669fe91f023f0','ACTIVO'),
('amiranda','0002','782a05e7e8bec129','53e08b28d90710c6251a558f6ef4e2572321c94cfa1f506347ba3a806a6a6b5e','ACTIVO'),
('gvivanco','0003','4c2f5a0987b94a30','76c4a37c48c7e4ab6d199c4cf764511c7542c17c41f1b80fcc63c32f3b09582c','ACTIVO');
INSERT INTO cliente (cod_cliente, apellido_paterno, apellido_materno, nombre, cedula, ciudad, direccion, telefono, email) VALUES
('00001','Coronel','Castillo','Eric','12345678','Quito','Av. 6 de Diciembre N33-10','0991111111','eric@correo.ec'),
('00002','Gonzales','Ramírez','Alberto','23456789','Quito','Calle Rumipamba 450','0992222222','alberto@correo.ec'),
('00003','Romero','Castillo','Carla','34567890','Sangolquí','Av. Luis Cordero 12','0993333333','carla@correo.ec'),
('00004','Cruz','Mena','Marta','45678901','Quito','Calle Whymper 200','0994444444','marta@correo.ec'),
('00005','Torres','Vega','Pedro','56789012','Cumbayá','Vía Interoceánica km 8','0995555555','pedro@correo.ec'),
('00006','Salazar','Ortiz','Sara','67890123','Quito','Av. Naciones Unidas 15','0996666666','sara@correo.ec');
INSERT INTO tipo_movimiento (cod_tipo, descripcion, accion) VALUES
('003','Depósito','INGRESO'),('004','Retiro','SALIDA'),('008','Transferencia','INGRESO'),('009','Transferencia','SALIDA');
INSERT INTO cuenta (cod_cuenta, cod_moneda, cod_sucursal, cod_empleado_crea, cod_cliente, saldo, fecha_creacion, estado, contador_mov) VALUES
('00100001','01','001','0001','00001',7000.00,'2026-01-05','ACTIVO',3),
('00100002','01','001','0001','00002',6800.00,'2026-01-06','ACTIVO',2),
('00200001','02','002','0002','00003',6000.00,'2026-01-07','ACTIVO',2),
('00200002','02','002','0002','00004',3500.00,'2026-01-08','ACTIVO',1),
('00100003','01','001','0003','00005',2200.00,'2026-01-09','ACTIVO',1),
('00100004','01','001','0003','00006',9000.00,'2026-01-10','ANULADO',0);
INSERT INTO movimiento (cod_cuenta, numero, fecha, cod_empleado, cod_tipo, importe, saldo_resultante, cuenta_referencia) VALUES
('00100001',1,'2026-01-05','0001','003',6000.00,6000.00,NULL),
('00100001',2,'2026-01-20','0001','003',2000.00,8000.00,NULL),
('00100001',3,'2026-02-02','0001','004',1000.00,7000.00,NULL),
('00100002',1,'2026-01-06','0001','003',7000.00,7000.00,NULL),
('00100002',2,'2026-02-10','0001','004',200.00,6800.00,NULL),
('00200001',1,'2026-01-07','0002','003',5000.00,5000.00,NULL),
('00200001',2,'2026-02-15','0002','003',1000.00,6000.00,NULL),
('00200002',1,'2026-01-08','0002','003',3500.00,3500.00,NULL),
('00100003',1,'2026-01-09','0003','003',2200.00,2200.00,NULL);
