package net.datatecsolution.admintools.core;

import org.junit.Test;

import java.math.BigDecimal;

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
}
