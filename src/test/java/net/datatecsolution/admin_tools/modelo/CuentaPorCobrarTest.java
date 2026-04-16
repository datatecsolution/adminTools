package net.datatecsolution.admin_tools.modelo;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CuentaPorCobrarTest {

    private CuentaPorCobrar cuenta;

    @Before
    public void setUp() {
        cuenta = new CuentaPorCobrar();
    }

    @Test
    public void valoresPorDefecto() {
        assertEquals(-1, cuenta.getNoReguistro());
        assertEquals("", cuenta.getFecha());
        assertEquals("", cuenta.getDescripcion());
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getCredito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getDebito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getSaldo()));
    }

    @Test
    public void setCreditoAcumula() {
        cuenta.setCredito(new BigDecimal("100"));
        cuenta.setCredito(new BigDecimal("50"));
        assertEquals(0, new BigDecimal("150").compareTo(cuenta.getCredito()));
    }

    @Test
    public void setDebitoAcumula() {
        cuenta.setDebito(new BigDecimal("80"));
        cuenta.setDebito(new BigDecimal("20"));
        assertEquals(0, new BigDecimal("100").compareTo(cuenta.getDebito()));
    }

    @Test
    public void setSaldoAcumula() {
        cuenta.setSaldo(new BigDecimal("200"));
        cuenta.setSaldo(new BigDecimal("-50"));
        assertEquals(0, new BigDecimal("150").compareTo(cuenta.getSaldo()));
    }

    @Test
    public void resetTotalesLimpiaAcumuladores() {
        cuenta.setCredito(new BigDecimal("100"));
        cuenta.setDebito(new BigDecimal("80"));
        cuenta.setSaldo(new BigDecimal("20"));

        cuenta.resetTotales();

        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getCredito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getDebito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.getSaldo()));
    }

    @Test
    public void descripcionCortaNoSeTrunca() {
        cuenta.setDescripcion("Pago abono factura #123");
        assertEquals("Pago abono factura #123", cuenta.getDescripcion());
    }

    @Test
    public void descripcionLargaSeAbreviaA250Caracteres() {
        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longDesc.append("a");
        }
        cuenta.setDescripcion(longDesc.toString());

        String result = cuenta.getDescripcion();
        assertEquals(250, result.length());
        assertTrue(result.endsWith("..."));
    }

    @Test
    public void descripcionEn250CaracteresNoSeTrunca() {
        StringBuilder exact = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            exact.append("b");
        }
        cuenta.setDescripcion(exact.toString());
        assertEquals(250, cuenta.getDescripcion().length());
    }

    @Test
    public void noReguistroYFechaSeSetean() {
        cuenta.setNoReguistro(42);
        cuenta.setFecha("2026-04-13");
        assertEquals(42, cuenta.getNoReguistro());
        assertEquals("2026-04-13", cuenta.getFecha());
    }
}
