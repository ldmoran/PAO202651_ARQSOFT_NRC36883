USE eurekabank;
SET NOCOUNT ON;
-- Datos iniciales de EurekaBank (usuarios con sal + SHA-256; clave de demostracion: Eureka2026!)
INSERT INTO moneda (cod_moneda, descripcion) VALUES (N'01',N'Soles'),(N'02',N'Dólares');
INSERT INTO sucursal (cod_sucursal, nombre, ciudad, direccion) VALUES (N'001',N'Quito Centro',N'Quito',N'Av. Amazonas N24-60'),(N'002',N'Sangolquí',N'Sangolquí',N'Av. General Enríquez 123');
INSERT INTO empleado (cod_empleado, apellido_paterno, apellido_materno, nombre, ciudad, direccion) VALUES
(N'0001',N'Morán',N'',N'Lenin',N'Quito',N'Quito'),(N'0002',N'Miranda',N'',N'Alison',N'Quito',N'Quito'),(N'0003',N'Vivanco',N'',N'Gabriel',N'Quito',N'Quito');
INSERT INTO usuario (usuario, cod_empleado, sal, clave_hash, estado) VALUES
(N'lmoran',N'0001',N'a1a6e7cbcfd7e9f1',N'86a357fd59c6b18cbe85d78f5a5df101258900952f576d7bf82669fe91f023f0',N'ACTIVO'),
(N'amiranda',N'0002',N'782a05e7e8bec129',N'53e08b28d90710c6251a558f6ef4e2572321c94cfa1f506347ba3a806a6a6b5e',N'ACTIVO'),
(N'gvivanco',N'0003',N'4c2f5a0987b94a30',N'76c4a37c48c7e4ab6d199c4cf764511c7542c17c41f1b80fcc63c32f3b09582c',N'ACTIVO');
INSERT INTO cliente (cod_cliente, apellido_paterno, apellido_materno, nombre, cedula, ciudad, direccion, telefono, email) VALUES
(N'00001',N'Coronel',N'Castillo',N'Eric',N'12345678',N'Quito',N'Av. 6 de Diciembre N33-10',N'0991111111',N'eric@correo.ec'),
(N'00002',N'Gonzales',N'Ramírez',N'Alberto',N'23456789',N'Quito',N'Calle Rumipamba 450',N'0992222222',N'alberto@correo.ec'),
(N'00003',N'Romero',N'Castillo',N'Carla',N'34567890',N'Sangolquí',N'Av. Luis Cordero 12',N'0993333333',N'carla@correo.ec'),
(N'00004',N'Cruz',N'Mena',N'Marta',N'45678901',N'Quito',N'Calle Whymper 200',N'0994444444',N'marta@correo.ec'),
(N'00005',N'Torres',N'Vega',N'Pedro',N'56789012',N'Cumbayá',N'Vía Interoceánica km 8',N'0995555555',N'pedro@correo.ec'),
(N'00006',N'Salazar',N'Ortiz',N'Sara',N'67890123',N'Quito',N'Av. Naciones Unidas 15',N'0996666666',N'sara@correo.ec');
INSERT INTO tipo_movimiento (cod_tipo, descripcion, accion) VALUES
(N'003',N'Depósito',N'INGRESO'),(N'004',N'Retiro',N'SALIDA'),(N'008',N'Transferencia',N'INGRESO'),(N'009',N'Transferencia',N'SALIDA');
INSERT INTO cuenta (cod_cuenta, cod_moneda, cod_sucursal, cod_empleado_crea, cod_cliente, saldo, fecha_creacion, estado, contador_mov) VALUES
(N'00100001',N'01',N'001',N'0001',N'00001',7000.00,N'2026-01-05',N'ACTIVO',3),
(N'00100002',N'01',N'001',N'0001',N'00002',6800.00,N'2026-01-06',N'ACTIVO',2),
(N'00200001',N'02',N'002',N'0002',N'00003',6000.00,N'2026-01-07',N'ACTIVO',2),
(N'00200002',N'02',N'002',N'0002',N'00004',3500.00,N'2026-01-08',N'ACTIVO',1),
(N'00100003',N'01',N'001',N'0003',N'00005',2200.00,N'2026-01-09',N'ACTIVO',1),
(N'00100004',N'01',N'001',N'0003',N'00006',9000.00,N'2026-01-10',N'ANULADO',0);
INSERT INTO movimiento (cod_cuenta, numero, fecha, cod_empleado, cod_tipo, importe, saldo_resultante, cuenta_referencia) VALUES
(N'00100001',1,N'2026-01-05',N'0001',N'003',6000.00,6000.00,NULL),
(N'00100001',2,N'2026-01-20',N'0001',N'003',2000.00,8000.00,NULL),
(N'00100001',3,N'2026-02-02',N'0001',N'004',1000.00,7000.00,NULL),
(N'00100002',1,N'2026-01-06',N'0001',N'003',7000.00,7000.00,NULL),
(N'00100002',2,N'2026-02-10',N'0001',N'004',200.00,6800.00,NULL),
(N'00200001',1,N'2026-01-07',N'0002',N'003',5000.00,5000.00,NULL),
(N'00200001',2,N'2026-02-15',N'0002',N'003',1000.00,6000.00,NULL),
(N'00200002',1,N'2026-01-08',N'0002',N'003',3500.00,3500.00,NULL),
(N'00100003',1,N'2026-01-09',N'0003',N'003',2200.00,2200.00,NULL);
