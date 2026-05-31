package net.datatecsolution.admintools.core;

import java.math.BigDecimal;
import java.util.List;

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

    private static final BigDecimal CIEN = new BigDecimal(100);
    private static final BigDecimal UNO = BigDecimal.ONE;
    private static final BigDecimal OTROS_IMP_FACTOR = new BigDecimal("0.04");

    /** Una línea de venta para el cálculo de totales (precio INCLUYE ISV). */
    public static final class LineaInput {
        private final BigDecimal cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal descuento;
        private final int porcentajeImpuesto;
        private final int tipoArticulo;

        public LineaInput(BigDecimal cantidad, BigDecimal precioUnitario,
                          BigDecimal descuento, int porcentajeImpuesto, int tipoArticulo) {
            this.cantidad = cantidad == null ? BigDecimal.ZERO : cantidad;
            this.precioUnitario = precioUnitario == null ? BigDecimal.ZERO : precioUnitario;
            this.descuento = descuento == null ? BigDecimal.ZERO : descuento;
            this.porcentajeImpuesto = porcentajeImpuesto;
            this.tipoArticulo = tipoArticulo;
        }
    }

    /** Totales del encabezado, desglosados por tasa de ISV. */
    public static final class Totales {
        private final BigDecimal subtotal;
        private final BigDecimal subtotalExcento;
        private final BigDecimal subtotal15;
        private final BigDecimal subtotal18;
        private final BigDecimal impuesto;
        private final BigDecimal isv18;
        private final BigDecimal otrosImpuestos;
        private final BigDecimal descuento;
        private final BigDecimal total;

        Totales(BigDecimal subtotal, BigDecimal subtotalExcento, BigDecimal subtotal15,
                BigDecimal subtotal18, BigDecimal impuesto, BigDecimal isv18,
                BigDecimal otrosImpuestos, BigDecimal descuento, BigDecimal total) {
            this.subtotal = subtotal;
            this.subtotalExcento = subtotalExcento;
            this.subtotal15 = subtotal15;
            this.subtotal18 = subtotal18;
            this.impuesto = impuesto;
            this.isv18 = isv18;
            this.otrosImpuestos = otrosImpuestos;
            this.descuento = descuento;
            this.total = total;
        }

        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getSubtotalExcento() { return subtotalExcento; }
        public BigDecimal getSubtotal15() { return subtotal15; }
        public BigDecimal getSubtotal18() { return subtotal18; }
        public BigDecimal getImpuesto() { return impuesto; }
        public BigDecimal getIsv18() { return isv18; }
        public BigDecimal getOtrosImpuestos() { return otrosImpuestos; }
        public BigDecimal getDescuento() { return descuento; }
        public BigDecimal getTotal() { return total; }
    }

    /**
     * Calcula los totales de una factura a partir de sus líneas, replicando
     * {@code FacturacionService.calcularTotalesDetalle} del Swing: los precios
     * INCLUYEN ISV, así que se separa la base (total/(1+%/100)) del impuesto, y
     * se acumula por tasa. Determinista; escala 2 HALF_EVEN.
     */
    public static Totales calcularTotales(List<LineaInput> lineas) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal subtotalExcento = BigDecimal.ZERO;
        BigDecimal subtotal15 = BigDecimal.ZERO;
        BigDecimal subtotal18 = BigDecimal.ZERO;
        BigDecimal impuesto = BigDecimal.ZERO;
        BigDecimal isv18 = BigDecimal.ZERO;
        BigDecimal otrosImpuestos = BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        if (lineas != null) {
            for (LineaInput l : lineas) {
                if (l.cantidad.signum() == 0 || l.precioUnitario.signum() == 0) {
                    continue;
                }
                BigDecimal totalItem = l.cantidad.multiply(l.precioUnitario)
                        .subtract(l.descuento.setScale(2, BigDecimal.ROUND_HALF_EVEN));

                BigDecimal porImpuesto = new BigDecimal(l.porcentajeImpuesto)
                        .divide(CIEN).add(UNO);
                BigDecimal base = totalItem.divide(porImpuesto, 2, BigDecimal.ROUND_HALF_EVEN);
                BigDecimal impuestoItem = totalItem.subtract(base);

                total = total.add(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

                if (l.porcentajeImpuesto == 0) {
                    subtotalExcento = subtotalExcento.add(base.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                } else if (l.porcentajeImpuesto == 15) {
                    impuesto = impuesto.add(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                    subtotal15 = subtotal15.add(base.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                } else if (l.porcentajeImpuesto == 18) {
                    isv18 = isv18.add(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                    subtotal18 = subtotal18.add(base.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                }

                if (l.tipoArticulo == 3) {
                    BigDecimal otros = base.multiply(OTROS_IMP_FACTOR)
                            .setScale(2, BigDecimal.ROUND_HALF_EVEN);
                    otrosImpuestos = otrosImpuestos.add(otros);
                    total = total.add(otros);
                }

                subtotal = subtotal.add(base.setScale(2, BigDecimal.ROUND_HALF_EVEN));
                descuento = descuento.add(l.descuento.setScale(2, BigDecimal.ROUND_HALF_EVEN));
            }
        }

        return new Totales(s2(subtotal), s2(subtotalExcento), s2(subtotal15), s2(subtotal18),
                s2(impuesto), s2(isv18), s2(otrosImpuestos), s2(descuento), s2(total));
    }

    private static BigDecimal s2(BigDecimal v) {
        return v.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }
}
