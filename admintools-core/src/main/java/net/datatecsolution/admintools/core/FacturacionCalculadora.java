package net.datatecsolution.admintools.core;

import java.math.BigDecimal;

/**
 * Logica pura de calculo financiero para facturas.
 *
 * Esta clase es la PRIMERA pieza extraida al modulo admintools-core
 * para compartir entre el Swing legacy (adminTools) y el backend
 * Spring Boot (admintools API).
 *
 * Reglas de diseno:
 * <ul>
 *   <li>Cero dependencias de DAOs, Swing, JPA, Spring.</li>
 *   <li>Inputs/outputs son tipos primitivos o BigDecimal — nada de
 *       entidades application-specific.</li>
 *   <li>Determinista: mismas entradas, misma salida. Sin estado,
 *       sin side-effects.</li>
 *   <li>Metodos estaticos para reuso desde cualquier capa sin DI.</li>
 * </ul>
 */
public final class FacturacionCalculadora {

    private FacturacionCalculadora() {
        // No instanciable — solo utilities.
    }

    /**
     * Calcula el monto absoluto de un descuento porcentual sobre una
     * linea de detalle.
     *
     * Formula: round( cantidad * precioVenta * (porcentaje/100) ),
     * con scale 0 y modo HALF_EVEN.
     *
     * Misma formula y rounding que el original en
     * CtlFacturarFrame.aplicarDescuentoPorcentaje (Swing) — bit-identico.
     *
     * @param cantidad     unidades de la linea (no nulo)
     * @param precioVenta  precio unitario
     * @param porcentaje   porcentaje a aplicar, escala 0..100 (ej. 15 = 15%)
     * @return monto del descuento, redondeado a 0 decimales con HALF_EVEN
     */
    public static BigDecimal calcularDescuentoPorcentaje(BigDecimal cantidad,
                                                        double precioVenta,
                                                        double porcentaje) {
        BigDecimal precio = new BigDecimal(precioVenta);
        BigDecimal totalItem = cantidad.multiply(precio);
        BigDecimal factor = new BigDecimal(porcentaje / 100);
        return totalItem.multiply(factor).setScale(0, BigDecimal.ROUND_HALF_EVEN);
    }
}
