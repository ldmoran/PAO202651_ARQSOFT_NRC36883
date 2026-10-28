package ec.edu.monster.modelos.utilidades.enums;

/** Codigos de resultado que devuelve el servicio (ver diccionario de datos). */
public enum CodigoResultado {

    OK_200("OK-200"),
    TOKEN_INVALIDO("AUTH-001"),
    SESION_EXPIRADA("AUTH-002"),
    CREDENCIALES_INCORRECTAS("AUTH-003"),
    UNIDAD_NO_SOPORTADA("UNIT-001"),
    DATOS_INCONSISTENTES("DATA-001"),
    ERROR_SERVIDOR("SRV-500");

    private final String codigo;

    CodigoResultado(String codigo) {
        this.codigo = codigo;
    }

    /** Texto que viaja en el mensaje SOAP, por ejemplo "AUTH-001". */
    public String getCodigo() {
        return codigo;
    }
}
