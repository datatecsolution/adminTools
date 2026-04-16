package net.datatecsolution.admin_tools.modelo;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CotizacionTest {

    @Test
    public void heredaDeFactura() {
        Cotizacion cotizacion = new Cotizacion();
        assertTrue(cotizacion instanceof Factura);
    }

    @Test
    public void hechaFacturaFalsaPorDefecto() {
        Cotizacion cotizacion = new Cotizacion();
        assertFalse(cotizacion.isHechaFactura());
    }

    @Test
    public void marcarComoHechaFactura() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setHechaFactura(true);
        assertTrue(cotizacion.isHechaFactura());
    }

    @Test
    public void cotizacionUsaAcumuladoresDeFactura() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setTotal(new BigDecimal("100"));
        cotizacion.setTotal(new BigDecimal("25"));
        assertEquals(0, new BigDecimal("125").compareTo(cotizacion.getTotal()));
    }
}
