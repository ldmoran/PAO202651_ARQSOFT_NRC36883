-- Opcional (endurecimiento): despues de crear las tablas, deja al usuario del servicio solo con lo necesario.
REVOKE ALL PRIVILEGES ON eurekabank.* FROM 'eureka_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON eurekabank.* TO 'eureka_app'@'localhost';
FLUSH PRIVILEGES;
