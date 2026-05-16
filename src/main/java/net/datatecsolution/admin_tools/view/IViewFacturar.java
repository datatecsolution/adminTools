package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFacturarFrame;
import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.view.dto.FacturaCabeceraData;
import net.datatecsolution.admin_tools.view.dto.FacturaClienteData;

import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

/**
 * Contrato View ↔ Controller del módulo de facturación.
 *
 * Esta interfaz declara únicamente los métodos que CtlFacturarFrame
 * consume sobre la vista (auditados al cierre de Fase 4 del plan de
 * desacople). Sirve para:
 *
 *   - Formalizar el contrato, evitando que el controller dependa de
 *     la clase concreta ViewFacturarFrame.
 *   - Preparar la migración futura a React/REST: estas mismas
 *     responsabilidades deben mapear a endpoints del API
 *     (admintools-pos consumirá admintools API).
 *
 * Los métodos lifecycle (setVisible, dispose, getTopLevelAncestor)
 * los hereda ViewFacturarFrame de JInternalFrame; los re-declaramos
 * acá para que el controller pueda invocarlos a través de la interfaz
 * sin hacer cast.
 */
public interface IViewFacturar {

    // ─── Lifecycle / window ─────────────────────────────────────────────────
    void setVisible(boolean visible);
    void dispose();
    Container getTopLevelAncestor();
    void conectarContralador(CtlFacturarFrame c);

    /**
     * Devuelve esta vista como Component para APIs Swing que lo requieren
     * (ej. JOptionPane parent, JasperReports viewer). El implementador
     * tipicamente devuelve {@code this} porque es un JInternalFrame.
     */
    Component asComponent();

    // ─── Cabecera ───────────────────────────────────────────────────────────
    FacturaCabeceraData getCabeceraData();
    void setCabeceraData(FacturaCabeceraData data);

    // ─── Cliente ────────────────────────────────────────────────────────────
    FacturaClienteData getClienteData();
    void setClienteData(FacturaClienteData data);
    boolean esCampoNombreCliente(Component c);
    void resetIdCliente();

    // ─── Búsqueda ───────────────────────────────────────────────────────────
    JTextField getTxtBuscar();
    String getTextoBusqueda();
    void setTextoBusqueda(String texto);
    void limpiarBusqueda();
    void enfocarBusqueda();
    void limpiarYEnfocarBusqueda();
    void marcarBusquedaNivelFact(boolean nivelFact);

    // ─── Detalle (tabla + filas) ────────────────────────────────────────────
    void agregarDetalle();
    void vaciarDetalles();
    void setDetalles(List<DetalleFactura> detalles);
    List<DetalleFactura> getDetalles();
    DetalleFactura getDetalle(int fila);
    void setArticuloDetalle(Articulo articulo);
    void setArticuloDetalle(Articulo articulo, int fila);
    void eliminarDetalle(int fila);
    void masCantidad(int fila);
    void restarCantidad(int fila);
    double buscarCantidadPorArticulo(Articulo articulo);
    int getCantidadFilasDetalle();
    Object getValorTabla(int fila, int columna);
    void refrescarTablaDetalle();
    int getFilaSeleccionada();
    void seleccionarFila(int fila);
    void enfocarCeldaTabla(int fila, int columna, int columnaInicio, int columnaFin);

    // ─── Botones / acciones ─────────────────────────────────────────────────
    void setEstadoBotonesNuevo();
    void setEstadoBotonesEditandoOrden();
    void setModoActualizarFactura();
    void ocultarPanelAcciones();
    boolean puedeGuardar();
    boolean puedeActualizar();
    void setEstadoFactura(boolean editando, int numeroFactura);

    // ─── Panel de órdenes pendientes ────────────────────────────────────────
    void addBotonPendiente(Factura newFactura, CtlFacturarFrame c);
    void eliminarBotones();
    void limpiarOrdenesGuardadas();
    Factura getOrdenSeleccionadaPanel();
    Factura buscarOrdenEnPanel(int idFactura);
    boolean seleccionarOrdenEnPanel(int idFactura);
    void seleccionarBotonNuevaFactura();
    void refrescarPanelGuardados();

    // ─── Totales ────────────────────────────────────────────────────────────
    void actualizarTotales(Factura factura);
    void resetTotales();

    // ─── Menú contextual ────────────────────────────────────────────────────
    void mostrarMenuContextual(Component invoker, int x, int y);
}
