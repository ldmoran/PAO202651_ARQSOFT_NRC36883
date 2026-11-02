package ec.edu.monster.pruebas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** Reloj que se mueve a mano: permite probar la expiracion de tokens sin esperar. */
public final class RelojManual extends Clock {

    private Instant ahora;

    public RelojManual(Instant inicio) {
        this.ahora = inicio;
    }

    public void avanzar(Duration tiempo) {
        ahora = ahora.plus(tiempo);
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return ahora;
    }
}
