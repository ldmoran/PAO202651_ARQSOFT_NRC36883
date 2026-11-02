package ec.edu.monster.controladores.rest;

final class AutorizacionHttp {

    private static final String ESQUEMA_BEARER = "Bearer ";

    private AutorizacionHttp() {
    }

    static String extraerToken(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, ESQUEMA_BEARER, 0, ESQUEMA_BEARER.length())) {
            return null;
        }
        return authorization.substring(ESQUEMA_BEARER.length()).trim();
    }
}
