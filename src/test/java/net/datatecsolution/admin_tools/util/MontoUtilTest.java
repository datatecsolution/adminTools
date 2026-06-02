package net.datatecsolution.admin_tools.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * Regresión del hotfix del NumberFormatException al contar efectivo en el
 * cierre de caja. Antes, un campo vacío o no numérico hacía
 * {@code new BigDecimal(getText())} -> NumberFormatException en cada tecla.
 */
public class MontoUtilTest {

    private static void assertMonto(String esperado, BigDecimal actual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(actual));
    }

    @Test
    public void parse_vacio_devuelveCero() {
        assertMonto("0", MontoUtil.parse(""));
    }

    @Test
    public void parse_null_devuelveCero() {
        assertMonto("0", MontoUtil.parse(null));
    }

    @Test
    public void parse_soloEspacios_devuelveCero() {
        assertMonto("0", MontoUtil.parse("   "));
    }

    @Test
    public void parse_noNumerico_devuelveCero() {
        assertMonto("0", MontoUtil.parse("abc"));
    }

    @Test
    public void parse_conComa_devuelveCero() {
        // "1,5" no es válido para BigDecimal (coma) -> 0 en vez de reventar.
        assertMonto("0", MontoUtil.parse("1,5"));
    }

    @Test
    public void parse_signoSolo_devuelveCero() {
        assertMonto("0", MontoUtil.parse("-"));
    }

    @Test
    public void parse_numeroValido() {
        assertMonto("12", MontoUtil.parse("12"));
        assertMonto("3.50", MontoUtil.parse("3.50"));
        assertMonto("12", MontoUtil.parse("  12  ")); // trim
    }

    /**
     * Reproduce el conteo de efectivo del cierre con campos vacíos / no
     * numéricos mezclados: NO debe lanzar y el total debe ser correcto.
     * Denominaciones: 1, 2, 5, 10, 20, 50, 100, 500.
     */
    @Test
    public void totalDenominaciones_conCamposVaciosONoNumericos_noRevientaYSuma() {
        String uno = "2";          // 2 x 1   = 2
        String dos = "";           // vacío   -> 0
        String cinco = "abc";      // inválido-> 0
        String diez = "1";         // 1 x 10  = 10
        String veinte = "0";       // 0
        String cincuenta = "1,5";  // coma    -> 0
        String cien = "";          // vacío   -> 0
        String quinientos = "1";   // 1 x 500 = 500

        BigDecimal total = MontoUtil.parse(uno)
                .add(MontoUtil.parse(dos).multiply(new BigDecimal(2)))
                .add(MontoUtil.parse(cinco).multiply(new BigDecimal(5)))
                .add(MontoUtil.parse(diez).multiply(new BigDecimal(10)))
                .add(MontoUtil.parse(veinte).multiply(new BigDecimal(20)))
                .add(MontoUtil.parse(cincuenta).multiply(new BigDecimal(50)))
                .add(MontoUtil.parse(cien).multiply(new BigDecimal(100)))
                .add(MontoUtil.parse(quinientos).multiply(new BigDecimal(500)));

        assertMonto("512", total); // 2 + 10 + 500
    }
}
