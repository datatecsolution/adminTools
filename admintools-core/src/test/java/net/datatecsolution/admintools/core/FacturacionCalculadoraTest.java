package net.datatecsolution.admintools.core;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FacturacionCalculadoraTest {

    @Test
    public void descuentoPorcentaje_calculoBasico() {
        // 2 unidades * 100.00 * 10% = 20
        BigDecimal resultado = FacturacionCalculadora.calcularDescuentoPorcentaje(
                new BigDecimal(2), 100.0, 10.0);
        assertEquals(new BigDecimal(20), resultado);
    }

    @Test
    public void descuentoPorcentaje_redondeoHalfEven() {
        // 1 unidad * 33.33 * 15% = 4.9995 -> redondea a 5 (HALF_EVEN, scale 0)
        BigDecimal resultado = FacturacionCalculadora.calcularDescuentoPorcentaje(
                new BigDecimal(1), 33.33, 15.0);
        assertEquals(new BigDecimal(5), resultado);
    }

    @Test
    public void descuentoPorcentaje_porcentajeCero() {
        BigDecimal resultado = FacturacionCalculadora.calcularDescuentoPorcentaje(
                new BigDecimal(5), 200.0, 0.0);
        assertEquals(new BigDecimal(0), resultado);
    }

    @Test
    public void descuentoPorcentaje_cantidadCero() {
        BigDecimal resultado = FacturacionCalculadora.calcularDescuentoPorcentaje(
                BigDecimal.ZERO, 100.0, 35.0);
        assertEquals(new BigDecimal(0), resultado);
    }

    // ----- calcularTotales (precio INCLUYE ISV) -----

    @Test
    public void totales_lineaIsv15_separaBaseEImpuesto() {
        // 1 x 115.00 al 15% -> base 100.00, impuesto 15.00, total 115.00
        FacturacionCalculadora.Totales t = FacturacionCalculadora.calcularTotales(
                Collections.singletonList(new FacturacionCalculadora.LineaInput(
                        new BigDecimal("1"), new BigDecimal("115.00"), BigDecimal.ZERO, 15, 1)));
        assertEquals(new BigDecimal("100.00"), t.getSubtotal15());
        assertEquals(new BigDecimal("15.00"), t.getImpuesto());
        assertEquals(new BigDecimal("100.00"), t.getSubtotal());
        assertEquals(new BigDecimal("115.00"), t.getTotal());
        assertEquals(new BigDecimal("0.00"), t.getSubtotalExcento());
    }

    @Test
    public void totales_lineaIsv18() {
        FacturacionCalculadora.Totales t = FacturacionCalculadora.calcularTotales(
                Collections.singletonList(new FacturacionCalculadora.LineaInput(
                        new BigDecimal("1"), new BigDecimal("118.00"), BigDecimal.ZERO, 18, 1)));
        assertEquals(new BigDecimal("100.00"), t.getSubtotal18());
        assertEquals(new BigDecimal("18.00"), t.getIsv18());
        assertEquals(new BigDecimal("118.00"), t.getTotal());
    }

    @Test
    public void totales_lineaExcenta() {
        FacturacionCalculadora.Totales t = FacturacionCalculadora.calcularTotales(
                Collections.singletonList(new FacturacionCalculadora.LineaInput(
                        new BigDecimal("2"), new BigDecimal("50.00"), BigDecimal.ZERO, 0, 1)));
        assertEquals(new BigDecimal("100.00"), t.getSubtotalExcento());
        assertEquals(new BigDecimal("0.00"), t.getImpuesto());
        assertEquals(new BigDecimal("100.00"), t.getTotal());
    }

    @Test
    public void totales_conDescuentoYMezclaDeTasas() {
        // L1: 2 x 115 al 15% con desc 30 -> totalItem 200, base 173.91, imp 26.09
        // L2: 1 x 100 excento -> base 100, total 100
        FacturacionCalculadora.Totales t = FacturacionCalculadora.calcularTotales(Arrays.asList(
                new FacturacionCalculadora.LineaInput(
                        new BigDecimal("2"), new BigDecimal("115.00"), new BigDecimal("30"), 15, 1),
                new FacturacionCalculadora.LineaInput(
                        new BigDecimal("1"), new BigDecimal("100.00"), BigDecimal.ZERO, 0, 1)));
        assertEquals(new BigDecimal("173.91"), t.getSubtotal15());
        assertEquals(new BigDecimal("26.09"), t.getImpuesto());
        assertEquals(new BigDecimal("100.00"), t.getSubtotalExcento());
        assertEquals(new BigDecimal("30.00"), t.getDescuento());
        assertEquals(new BigDecimal("300.00"), t.getTotal()); // 200 + 100
    }

    @Test
    public void totales_omiteLineasVacias() {
        FacturacionCalculadora.Totales t = FacturacionCalculadora.calcularTotales(
                Collections.singletonList(new FacturacionCalculadora.LineaInput(
                        BigDecimal.ZERO, new BigDecimal("100.00"), BigDecimal.ZERO, 15, 1)));
        assertEquals(new BigDecimal("0.00"), t.getTotal());
    }
}
