package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.Impuesto;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FacturacionServiceTest {

    private FacturacionService service;
    private Factura factura;

    @Before
    public void setUp() {
        service = new FacturacionService();
        factura = new Factura();
    }

    private DetalleFactura detalle(double cantidad, double precioVenta,
                                   String porcentajeImpuesto, int tipoArticulo,
                                   double descuentoItem) {
        Impuesto imp = new Impuesto();
        imp.setPorcentaje(porcentajeImpuesto);

        Articulo art = new Articulo();
        art.setId(1);
        art.setPrecioVenta(precioVenta);
        art.setImpuestoObj(imp);
        art.setTipoArticulo(tipoArticulo);

        DetalleFactura d = new DetalleFactura();
        d.setListArticulos(art);
        d.setCantidad(new BigDecimal(cantidad));
        d.setDescuentoItem(new BigDecimal(descuentoItem));
        return d;
    }

    @Test
    public void calcularTotalesDetalleConImpuesto15() {
        DetalleFactura d = detalle(2, 100, "15", 1, 0);

        service.calcularTotalesDetalle(factura, d);

        assertEquals(0, new BigDecimal("200.00").compareTo(factura.getTotal()));
        assertEquals(0, new BigDecimal("173.91").compareTo(factura.getSubTotal15()));
        assertEquals(0, new BigDecimal("26.09").compareTo(factura.getTotalImpuesto()));
        assertEquals(0, new BigDecimal("173.91").compareTo(d.getSubTotal()));
        assertEquals(0, new BigDecimal("26.09").compareTo(d.getImpuesto()));
        assertEquals(0, new BigDecimal("200.00").compareTo(d.getTotal()));
    }

    @Test
    public void calcularTotalesDetalleConImpuesto18() {
        DetalleFactura d = detalle(1, 118, "18", 1, 0);

        service.calcularTotalesDetalle(factura, d);

        assertEquals(0, new BigDecimal("118.00").compareTo(factura.getTotal()));
        assertEquals(0, new BigDecimal("100.00").compareTo(factura.getSubTotal18()));
        assertEquals(0, new BigDecimal("18.00").compareTo(factura.getTotalImpuesto18()));
    }

    @Test
    public void calcularTotalesDetalleExentoUsaSubTotalExcento() {
        DetalleFactura d = detalle(3, 50, "0", 1, 0);

        service.calcularTotalesDetalle(factura, d);

        assertEquals(0, new BigDecimal("150.00").compareTo(factura.getTotal()));
        assertEquals(0, new BigDecimal("150.00").compareTo(factura.getSubTotalExcento()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotalImpuesto()));
    }

    @Test
    public void calcularTotalesDetalleAplicaDescuentoItem() {
        DetalleFactura d = detalle(1, 115, "15", 1, 15);

        service.calcularTotalesDetalle(factura, d);

        assertEquals(0, new BigDecimal("100.00").compareTo(factura.getTotal()));
        assertEquals(0, new BigDecimal("15.00").compareTo(factura.getTotalDescuento()));
    }

    @Test
    public void calcularTotalesDetalleTipoArticulo3SumaOtrosImpuestos() {
        DetalleFactura d = detalle(1, 115, "15", 3, 0);

        service.calcularTotalesDetalle(factura, d);

        assertEquals(0, new BigDecimal("4.00").compareTo(factura.getTotalOtrosImpuesto1()));
        assertEquals(0, new BigDecimal("119.00").compareTo(factura.getTotal()));
    }

    @Test
    public void calcularTotalesIgnoraDetalleConArticuloIdMenosUno() {
        DetalleFactura placeholder = detalle(1, 100, "15", 1, 0);
        placeholder.getArticulo().setId(-1);

        service.calcularTotales(factura, Arrays.asList(placeholder));

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getSubTotal()));
    }

    @Test
    public void calcularTotalesIgnoraDetalleConCantidadCero() {
        DetalleFactura d = detalle(0, 100, "15", 1, 0);

        service.calcularTotales(factura, Arrays.asList(d));

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()));
    }

    @Test
    public void calcularTotalesIgnoraDetalleConPrecioCero() {
        DetalleFactura d = detalle(1, 0, "15", 1, 0);

        service.calcularTotales(factura, Arrays.asList(d));

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()));
    }

    @Test
    public void calcularTotalesResetEntreLlamadas() {
        DetalleFactura d = detalle(1, 115, "15", 1, 0);

        service.calcularTotales(factura, Arrays.asList(d));
        assertEquals(0, new BigDecimal("115.00").compareTo(factura.getTotal()));

        service.calcularTotales(factura, Arrays.asList(d));
        assertEquals(0, new BigDecimal("115.00").compareTo(factura.getTotal()));
    }

    @Test
    public void calcularTotalesSumaVariosDetallesValidos() {
        DetalleFactura d15 = detalle(1, 115, "15", 1, 0);
        DetalleFactura d18 = detalle(1, 118, "18", 1, 0);
        DetalleFactura dExento = detalle(1, 50, "0", 1, 0);

        service.calcularTotales(factura, Arrays.asList(d15, d18, dExento));

        assertEquals(0, new BigDecimal("283.00").compareTo(factura.getTotal()));
        assertEquals(0, new BigDecimal("15.00").compareTo(factura.getTotalImpuesto()));
        assertEquals(0, new BigDecimal("18.00").compareTo(factura.getTotalImpuesto18()));
        assertEquals(0, new BigDecimal("50.00").compareTo(factura.getSubTotalExcento()));
    }

    @Test
    public void validarFacturaRechazaDetallesNull() {
        assertFalse(service.validarFactura(null, new Cliente()));
    }

    @Test
    public void validarFacturaRechazaListaConSoloPlaceholder() {
        List<DetalleFactura> uno = new ArrayList<>();
        uno.add(detalle(1, 100, "15", 1, 0));
        assertFalse(service.validarFactura(uno, new Cliente()));
    }

    @Test
    public void validarFacturaRechazaClienteNull() {
        List<DetalleFactura> dos = Arrays.asList(
                detalle(1, 100, "15", 1, 0),
                detalle(1, 100, "15", 1, 0));
        assertFalse(service.validarFactura(dos, null));
    }

    @Test
    public void validarFacturaAceptaCuandoHayMasDeUnDetalleYCliente() {
        List<DetalleFactura> dos = Arrays.asList(
                detalle(1, 100, "15", 1, 0),
                detalle(1, 100, "15", 1, 0));
        assertTrue(service.validarFactura(dos, new Cliente()));
    }

    @Test
    public void prepararFacturaCreditoSeteaCamposCredito() {
        service.prepararFacturaCredito(factura);

        assertEquals(3, factura.getTipoPago());
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getCambio()));
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getPago()));
        assertEquals(Integer.valueOf(2), factura.getTipoFactura());
    }
}
