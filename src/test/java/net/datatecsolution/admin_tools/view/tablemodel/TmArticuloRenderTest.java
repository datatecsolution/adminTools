package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorArticulos;
import org.junit.Test;

import javax.swing.JTable;
import java.awt.Color;
import java.awt.Component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * US-120 — contrato de columnas de TmArticulo y regresión del renderer.
 *
 * Contexto: al insertar la columna Disponible (índice 6), la columna oculta
 * Estado se corrió al 7. TablaRenderizadorArticulos la leía con índice fijo 6
 * y cast a String → ClassCastException (Double→String) en CADA celda: la
 * búsqueda de artículos de facturación quedaba muerta (visto en los logs del
 * 2026-07-29). Estas pruebas fijan el contrato para que no vuelva a pasar.
 */
public class TmArticuloRenderTest {

    private Articulo articulo(int id, double existencia, double disponible, boolean alta) {
        Articulo a = new Articulo();
        a.setId(id);
        a.setArticulo("Articulo " + id);
        a.getCategoria().setDescripcion("CAT");
        a.getImpuestoObj().setPorcentaje("15");
        a.setPrecioVenta(10.0);
        a.setExistencia(existencia);
        a.setDisponible(disponible);
        a.setEstado(alta);
        return a;
    }

    @Test
    public void contratoColumnas_disponibleVisible_estadoOculto() {
        TmArticulo tm = new TmArticulo();
        tm.agregarArticulo(articulo(1, 7, 6, true));

        // 7 columnas visibles (0..6); Estado queda FUERA del count (oculta).
        assertEquals(7, tm.getColumnCount());
        assertEquals("Disponible", tm.getColumnName(TmArticulo.COL_DISPONIBLE));
        assertEquals("Estado", tm.getColumnName(TmArticulo.COL_ESTADO));
        assertTrue(TmArticulo.COL_ESTADO >= tm.getColumnCount());

        // Tipos del contrato: Disponible numérico, Estado etiqueta Alta/Baja.
        assertEquals(6.0, ((Number) tm.getValueAt(0, TmArticulo.COL_DISPONIBLE)).doubleValue(), 0.001);
        assertEquals("Alta", tm.getValueAt(0, TmArticulo.COL_ESTADO));
    }

    @Test
    public void disponible_reflejaReservaDePedidos() {
        TmArticulo tm = new TmArticulo();
        tm.agregarArticulo(articulo(319697, 7, 6, true)); // 1 unidad en pedidos

        assertEquals(7.0, ((Number) tm.getValueAt(0, 5)).doubleValue(), 0.001); // físico intacto
        assertEquals(6.0, ((Number) tm.getValueAt(0, TmArticulo.COL_DISPONIBLE)).doubleValue(), 0.001);
    }

    /** La regresión exacta: renderizar TODAS las celdas visibles no revienta. */
    @Test
    public void renderer_noRevientaConLaColumnaDisponible() {
        TmArticulo tm = new TmArticulo();
        tm.agregarArticulo(articulo(1, 7, 6, true));
        tm.agregarArticulo(articulo(2, 3, 3, false));
        JTable tabla = new JTable(tm);
        TablaRenderizadorArticulos renderer = new TablaRenderizadorArticulos();

        for (int fila = 0; fila < tm.getRowCount(); fila++) {
            for (int col = 0; col < tm.getColumnCount(); col++) {
                // Antes del fix: ClassCastException Double→String en cada celda.
                Component c = renderer.getTableCellRendererComponent(
                        tabla, tm.getValueAt(fila, col), false, false, fila, col);
                assertTrue(c instanceof Component);
            }
        }
    }

    /** El pintado de bajas sigue funcionando leyendo Estado de su nueva posición. */
    @Test
    public void renderer_pintaDeRojoLasBajas() {
        TmArticulo tm = new TmArticulo();
        tm.agregarArticulo(articulo(1, 7, 6, true));   // Alta
        tm.agregarArticulo(articulo(2, 3, 3, false));  // Baja
        JTable tabla = new JTable(tm);
        TablaRenderizadorArticulos renderer = new TablaRenderizadorArticulos();

        Component alta = renderer.getTableCellRendererComponent(tabla, tm.getValueAt(0, 1), false, false, 0, 1);
        Component baja = renderer.getTableCellRendererComponent(tabla, tm.getValueAt(1, 1), false, false, 1, 1);

        assertTrue(!Color.RED.equals(alta.getBackground()));
        assertEquals(Color.RED, baja.getBackground());
    }
}
