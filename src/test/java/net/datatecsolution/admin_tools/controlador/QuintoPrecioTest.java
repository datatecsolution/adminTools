package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 5° precio en la facturacion del Swing (docs/analisis-nuevo-precio-facturacion-swing.md).
 *
 * «Seleccionar precio» ofrece solo precios que existen, y si una linea no
 * tiene el elegido lo dice en vez de fallar en silencio.
 */
public class QuintoPrecioTest {

	private static PrecioArticulo precio(int codigo, String descripcion, String valor) {
		PrecioArticulo p = new PrecioArticulo();
		p.setCodigoPrecio(codigo);
		p.setDecripcion(descripcion);
		p.setPrecio(new BigDecimal(valor));
		return p;
	}

	private static Articulo articuloCon(PrecioArticulo... precios) {
		Articulo a = new Articulo();
		a.setPreciosVenta(new ArrayList<PrecioArticulo>(Arrays.asList(precios)));
		a.setPrecioVenta(precios[0].getPrecio().doubleValue());
		return a;
	}

	@Test
	public void setPrecioAplicaElQuintoPrecioSiLaLineaLoTiene() {
		Articulo a = articuloCon(precio(1, "Publico", "100"), precio(2, "Especial", "90"), precio(5, "Ruta", "85"));

		assertTrue(a.setPrecio(precio(5, "Ruta", "0")));
		assertEquals(85.0, a.getPrecioVenta(), 0.001);
	}

	@Test
	public void setPrecioDevuelveFalseYNoTocaLaLineaSiNoTieneEsePrecio() {
		Articulo a = articuloCon(precio(1, "Publico", "100"), precio(2, "Especial", "90"));

		assertFalse(a.setPrecio(precio(5, "Ruta", "0")));
		assertEquals(100.0, a.getPrecioVenta(), 0.001);
	}

	@Test
	public void lasFlechasSiguenDesdeElPrecioElegido() {
		Articulo a = articuloCon(precio(1, "Publico", "100"), precio(2, "Especial", "90"),
				precio(3, "Mayorista", "80"), precio(5, "Ruta", "85"));

		a.setPrecio(precio(3, "Mayorista", "0"));
		a.netPrecio();
		assertEquals(85.0, a.getPrecioVenta(), 0.001);
		a.lastPrecio();
		assertEquals(80.0, a.getPrecioVenta(), 0.001);
	}

	@Test
	public void unirDejaUnPrecioPorCodigoOrdenadoPorCodigo() {
		List<PrecioArticulo> linea1 = Arrays.asList(precio(1, "Publico", "100"), precio(5, "Ruta", "85"));
		List<PrecioArticulo> linea2 = Arrays.asList(precio(1, "Publico", "50"), precio(4, "Costos", "30"), precio(2, "Especial", "45"));

		List<List<PrecioArticulo>> lineas = new ArrayList<List<PrecioArticulo>>();
		lineas.add(linea1);
		lineas.add(linea2);
		lineas.add(null);
		List<PrecioArticulo> union = CtlSelectPrecio.unir(lineas);

		assertEquals(4, union.size());
		assertEquals(1, union.get(0).getCodigoPrecio());
		assertEquals(2, union.get(1).getCodigoPrecio());
		assertEquals(4, union.get(2).getCodigoPrecio());
		assertEquals(5, union.get(3).getCodigoPrecio());
	}
}
