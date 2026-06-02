package net.datatecsolution.admin_tools.util;

import java.math.BigDecimal;

/**
 * Parseo tolerante de montos tecleados por el usuario. Un campo vacío, en
 * blanco o con texto no numérico devuelve 0 en vez de lanzar
 * NumberFormatException (causa del crash al contar efectivo en el cierre de
 * caja: ViewCuentaEfectivo.calcularTotal / CtlContarEfectivo.setTotal).
 */
public final class MontoUtil {

    private MontoUtil() {
    }

    public static BigDecimal parse(String texto) {
        if (texto == null) {
            return BigDecimal.ZERO;
        }
        String t = texto.trim();
        if (t.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
