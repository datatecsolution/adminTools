package net.datatecsolution.admin_tools.modelo;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NumberToLetterConverterTest {

    @Test
    public void ceroDevuelveTextoConCero() {
        String result = NumberToLetterConverter.convertNumberToLetter(0);
        assertTrue(result, result.contains("CERO"));
        assertTrue(result, result.contains("LEMPIRAS"));
    }

    @Test
    public void unoDevuelveUn() {
        String result = NumberToLetterConverter.convertNumberToLetter(1);
        assertTrue(result, result.contains("UN"));
        assertTrue(result, result.contains("LEMPIRAS"));
    }

    @Test
    public void cienDevuelveCien() {
        String result = NumberToLetterConverter.convertNumberToLetter(100);
        assertTrue(result, result.contains("CIEN"));
        assertTrue(result, result.contains("LEMPIRAS"));
    }

    @Test
    public void milDevuelveMil() {
        String result = NumberToLetterConverter.convertNumberToLetter(1000);
        assertTrue(result, result.contains("MIL"));
    }

    @Test
    public void unMillonDevuelveUnMillon() {
        String result = NumberToLetterConverter.convertNumberToLetter(1_000_000);
        assertTrue(result, result.contains("UN MILLON"));
    }

    @Test
    public void variosMillonesDevuelveMillones() {
        String result = NumberToLetterConverter.convertNumberToLetter(2_000_000);
        assertTrue(result, result.contains("MILLONES"));
    }

    @Test
    public void centavoSingularCuandoEsUno() {
        String result = NumberToLetterConverter.convertNumberToLetter(1.01);
        assertTrue(result, result.contains("UN CENTAVO"));
    }

    @Test
    public void centavosPluralCuandoSonVarios() {
        String result = NumberToLetterConverter.convertNumberToLetter(50.25);
        assertTrue(result, result.contains("CENTAVOS"));
    }

    @Test
    public void stringOverloadDelega() {
        String result = NumberToLetterConverter.convertNumberToLetter("123.45");
        assertNotNull(result);
        assertTrue(result, result.contains("LEMPIRAS"));
    }

    @Test(expected = NumberFormatException.class)
    public void negativoLanzaExcepcion() {
        NumberToLetterConverter.convertNumberToLetter(-1);
    }

    @Test(expected = NumberFormatException.class)
    public void mayorQueLimiteLanzaExcepcion() {
        NumberToLetterConverter.convertNumberToLetter(1_000_000_000d);
    }

    @Test
    public void numeroGrandeDentroDeRangoEsAceptado() {
        String result = NumberToLetterConverter.convertNumberToLetter(9_999_999d);
        assertNotNull(result);
        assertTrue(result, result.contains("LEMPIRAS"));
        assertTrue(result, result.contains("MIL"));
    }
}
