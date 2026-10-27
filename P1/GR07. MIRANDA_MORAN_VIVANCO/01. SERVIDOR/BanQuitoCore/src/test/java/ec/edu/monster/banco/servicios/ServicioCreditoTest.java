package ec.edu.monster.banco.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.banco.BaseDatosPrueba;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import ec.edu.monster.banco.modelos.DetalleMontoMaximo;
import ec.edu.monster.banco.modelos.EvaluacionCredito;
import ec.edu.monster.banco.modelos.ResultadoCredito;
import ec.edu.monster.banco.repositorios.CoreRepositorio;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioCreditoTest {

    private ServicioCredito servicio;

    @BeforeEach
    void preparar(org.junit.jupiter.api.TestInfo info) throws SQLException {
        String bd = "core_" + Math.abs(info.getDisplayName().hashCode()) + "_" + System.nanoTime();
        servicio = new ServicioCredito(new CoreRepositorio(BaseDatosPrueba.crear(bd)), BaseDatosPrueba.RELOJ);
    }

    @Test
    void clienteConDepositoRecienteSinCreditoEsSujeto() {
        EvaluacionCredito e = servicio.evaluar("0101010101");
        assertTrue(e.isSujetoCredito(), e.getMotivo());
        assertEquals(1, e.getCodCliente());
    }

    @Test
    void cedulaInexistenteNoEsSujeto() {
        assertFalse(servicio.evaluar("9999999999").isSujetoCredito());
    }

    @Test
    void hombreMenorDe25NoEsSujeto() {
        assertFalse(servicio.evaluar("0202020202").isSujetoCredito());
    }

    @Test
    void mujerJovenSiEsSujeto() {
        assertTrue(servicio.evaluar("0606060606").isSujetoCredito());
    }

    @Test
    void hombreDe25ExactosSiEsSujeto() {
        assertTrue(servicio.evaluar("0505050505").isSujetoCredito());
    }

    @Test
    void sinDepositoEnElUltimoMesNoEsSujeto() {
        assertFalse(servicio.evaluar("0303030303").isSujetoCredito());
    }

    @Test
    void conCreditoActivoNoEsSujeto() {
        EvaluacionCredito e = servicio.evaluar("0404040404");
        assertFalse(e.isSujetoCredito());
        assertTrue(e.getMotivo().contains("credito activo"));
    }

    @Test
    void montoMaximoUsaSoloLosUltimosTresMeses() {
        DetalleMontoMaximo d = servicio.montoMaximo("0101010101");
        assertEquals(1000.00, d.getPromedioDepositos(), 0.0);
        assertEquals(400.00, d.getPromedioRetiros(), 0.0);
        assertEquals(1080.00, d.getMontoMaximo(), 0.0);
    }

    @Test
    void noSujetoTieneMontoMaximoCero() {
        assertEquals(0.0, servicio.montoMaximo("0202020202").getMontoMaximo(), 0.0);
    }

    @Test
    void otorgaCreditoYGuardaLaTabla() {
        ResultadoCredito r = servicio.otorgar("0101010101", 450, 6);
        assertTrue(r.isAprobado(), r.getMensaje());
        assertEquals(78.65, r.getCuotaFija(), 0.0);
        List<CuotaAmortizacion> t = servicio.tabla(r.getCodCredito());
        assertEquals(7, t.size());
        assertEquals(0, t.get(0).getNumCuota());
        assertEquals(0.0, t.get(6).getSaldo(), 0.0);
        assertEquals(1, servicio.creditosDe("0101010101").size());
    }

    @Test
    void despuesDeOtorgarYaTieneCreditoActivo() {
        assertTrue(servicio.otorgar("0101010101", 450, 6).isAprobado());
        ResultadoCredito segundo = servicio.otorgar("0101010101", 100, 3);
        assertFalse(segundo.isAprobado());
    }

    @Test
    void rechazaMontoMayorAlMaximo() {
        ResultadoCredito r = servicio.otorgar("0101010101", 1080.01, 6);
        assertFalse(r.isAprobado());
        assertEquals(0, r.getCodCredito());
        assertTrue(servicio.creditosDe("0101010101").isEmpty());
    }

    @Test
    void aceptaMontoIgualAlMaximo() {
        assertTrue(servicio.otorgar("0101010101", 1080.00, 18).isAprobado());
    }

    @Test
    void rechazaPlazoFueraDeRango() {
        assertFalse(servicio.otorgar("0101010101", 450, 2).isAprobado());
        assertFalse(servicio.otorgar("0101010101", 450, 19).isAprobado());
    }

    @Test
    void rechazaMontoCeroONegativo() {
        assertFalse(servicio.otorgar("0101010101", 0, 6).isAprobado());
        assertFalse(servicio.otorgar("0101010101", -5, 6).isAprobado());
    }

    @Test
    void tablaDeCreditoInexistenteEsVacia() {
        assertTrue(servicio.tabla(999).isEmpty());
    }
}
