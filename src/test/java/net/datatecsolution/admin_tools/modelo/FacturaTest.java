package net.datatecsolution.admin_tools.modelo;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FacturaTest {

    private Factura factura;

    @Before
    public void setUp() {
        factura = new Factura();
    }

    @Test
    public void valoresPorDefecto() {
        assertEquals(Integer.valueOf(0), factura.getIdFactura());
        assertEquals(Integer.valueOf(1), factura.getTipoFactura());
        assertEquals(1, factura.getCodigoCaja());
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getSubTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotalImpuesto()));
        assertNotNull(factura.getDetalles());
        assertTrue(factura.getDetalles().isEmpty());
    }

    @Test
    public void setTotalAcumula() {
        factura.setTotal(new BigDecimal("100.00"));
        factura.setTotal(new BigDecimal("50.00"));
        assertEquals(0, new BigDecimal("150.00").compareTo(factura.getTotal()));
    }

    @Test
    public void setSubTotalAcumula() {
        factura.setSubTotal(new BigDecimal("30"));
        factura.setSubTotal(new BigDecimal("20"));
        assertEquals(0, new BigDecimal("50").compareTo(factura.getSubTotal()));
    }

    @Test
    public void setTotalImpuestoAcumula() {
        factura.setTotalImpuesto(new BigDecimal("10"));
        factura.setTotalImpuesto(new BigDecimal("5"));
        assertEquals(0, new BigDecimal("15").compareTo(factura.getTotalImpuesto()));
    }

    @Test
    public void setTotalDescuentoAcumula() {
        factura.setTotalDescuento(new BigDecimal("2.50"));
        factura.setTotalDescuento(new BigDecimal("2.50"));
        assertEquals(0, new BigDecimal("5.00").compareTo(factura.getTotalDescuento()));
    }

    @Test
    public void resetTotalesDejaTodoEnCero() {
        factura.setTotal(new BigDecimal("999"));
        factura.setSubTotal(new BigDecimal("999"));
        factura.setTotalImpuesto(new BigDecimal("999"));
        factura.setTotalDescuento(new BigDecimal("999"));

        factura.resetTotales();

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getSubTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotalImpuesto()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotalDescuento()));
    }

    @Test
    public void codigoCajaSeSeteaYRecupera() {
        factura.setCodigoCaja(7);
        assertEquals(7, factura.getCodigoCaja());
    }

    @Test
    public void tipoFacturaCreditoEs2() {
        factura.setTipoFactura(2);
        assertEquals(Integer.valueOf(2), factura.getTipoFactura());
    }

    @Test
    public void saldoCreditoSeSeteaSinAcumular() {
        factura.setSaldo(new BigDecimal("500"));
        factura.setSaldo(new BigDecimal("300"));
        assertEquals(0, new BigDecimal("300").compareTo(factura.getSaldo()));
    }
}
