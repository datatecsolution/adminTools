package net.datatecsolution.admin_tools.modelo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ConfigUserFacturacionTest {

    private ConfigUserFacturacion config;

    @Before
    public void setUp() {
        config = new ConfigUserFacturacion();
    }

    @Test
    public void rutaCobroEnBusquedaInicializadaPorConstructor() {
        RutaCobro ruta = config.getRutaCobroEnBusqueda();
        assertNotNull(ruta);
        assertEquals(1, ruta.getCodigo());
    }

    @Test
    public void vendedorEnBusquedaInicializadoPorConstructor() {
        Empleado empleado = config.getVendedorEnBusqueda();
        assertNotNull(empleado);
        assertEquals(1, empleado.getCodigo());
    }

    @Test
    public void departamentoEnBusquedaInicializadoPorConstructor() {
        Departamento departamento = config.getDepartEnBusqueda();
        assertNotNull(departamento);
        assertEquals(1, departamento.getId());
    }

    @Test
    public void valoresBooleanosPorDefectoEnFalse() {
        assertFalse(config.isPwdDescuento());
        assertFalse(config.isPwdPrecio());
        assertFalse(config.isActivarBusquedaFacturacion());
        assertFalse(config.isAgregarClienteCredito());
        assertFalse(config.isFacturarSinInventario());
    }

    @Test
    public void formatoFacturaPorDefectoEsTiket() {
        assertEquals("tiket", config.getFormatoFactura());
        assertEquals("tiket", config.getFormatoFacturaCredito());
    }

    @Test
    public void copiasFacturasPorDefectoEsUno() {
        assertEquals(1, config.getCopiasFacturas());
    }

    @Test
    public void flagCreditoSePuedeActivar() {
        config.setAgregarClienteCredito(true);
        config.setFormatoFacturaCredito("carta");
        assertEquals(true, config.isAgregarClienteCredito());
        assertEquals("carta", config.getFormatoFacturaCredito());
    }

    @Test
    public void rutaCobroEnBusquedaSePuedeReemplazar() {
        RutaCobro nueva = new RutaCobro();
        nueva.setCodigo(5);
        config.setRutaCobroEnBusqueda(nueva);
        assertEquals(5, config.getRutaCobroEnBusqueda().getCodigo());
    }
}
