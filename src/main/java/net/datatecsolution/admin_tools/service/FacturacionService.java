package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FacturacionService {

	private final FacturaDao facturaDao;
	private final FacturaOrdenVentaDao facturaOrdenesDao;
	private final PrecioArticuloDao preciosDao;
	private final DetalleFacturaOrdenDao detallesOrdenDao;

	public FacturacionService() {
		facturaDao = new FacturaDao();
		facturaOrdenesDao = new FacturaOrdenVentaDao();
		preciosDao = new PrecioArticuloDao();
		detallesOrdenDao = new DetalleFacturaOrdenDao();
	}

	public String getFechaSistema() {
		return facturaDao.getFechaSistema();
	}

	public Integer getIdFacturaGuardada() {
		return facturaDao.getIdFacturaGuardada();
	}

	public Integer getIdOrdenGuardada() {
		return facturaOrdenesDao.getIdFacturaGuardada();
	}

	public void calcularTotalesDetalle(Factura factura, DetalleFactura detalle) {
		BigDecimal cantidad = detalle.getCantidad();
		BigDecimal precioVenta = new BigDecimal(detalle.getArticulo().getPrecioVenta());

		BigDecimal totalItem = cantidad.multiply(precioVenta);

		BigDecimal des = detalle.getDescuentoItem();
		totalItem = totalItem.subtract(des.setScale(2, BigDecimal.ROUND_HALF_EVEN));

		BigDecimal porcentaImpuesto = new BigDecimal(
				detalle.getArticulo().getImpuestoObj().getPorcentaje());
		BigDecimal porImpuesto = porcentaImpuesto.divide(new BigDecimal(100));
		porImpuesto = porImpuesto.add(new BigDecimal(1));

		BigDecimal totalsiniva = totalItem.divide(porImpuesto, 2, BigDecimal.ROUND_HALF_EVEN);

		BigDecimal impuestoItem = totalItem.subtract(totalsiniva);

		factura.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

		if (porcentaImpuesto.intValue() == 0) {
			factura.setSubTotalExcento(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		} else if (porcentaImpuesto.intValue() == 15) {
			factura.setTotalImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			factura.setSubTotal15(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		} else if (porcentaImpuesto.intValue() == 18) {
			factura.setTotalImpuesto18(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			factura.setSubTotal18(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		if (detalle.getArticulo().getTipoArticulo() == 3) {
			BigDecimal totalOtrosImp = totalsiniva.multiply(new BigDecimal(0.04));
			factura.setTotalOtrosImpuesto(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
			factura.setTotal(totalOtrosImp.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		factura.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		factura.setTotalDescuento(detalle.getDescuentoItem().setScale(2, BigDecimal.ROUND_HALF_EVEN));

		detalle.setSubTotal(totalsiniva.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		detalle.setImpuesto(impuestoItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		detalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));
	}

	public void calcularTotales(Factura factura, List<DetalleFactura> detalles) {
		factura.resetTotales();
		for (DetalleFactura detalle : detalles) {
			if (detalle.getArticulo().getId() != -1
					&& detalle.getCantidad().doubleValue() != 0
					&& detalle.getArticulo().getPrecioVenta() != 0) {
				calcularTotalesDetalle(factura, detalle);
			}
		}
	}

	public BigDecimal calcularDescuentoPorcentaje(DetalleFactura detalle, double porcentaje) {
		BigDecimal cantidad = detalle.getCantidad();
		BigDecimal precioVenta = new BigDecimal(detalle.getArticulo().getPrecioVenta());
		BigDecimal totalItem = cantidad.multiply(precioVenta);
		BigDecimal factor = new BigDecimal(porcentaje / 100);
		return totalItem.multiply(factor).setScale(0, BigDecimal.ROUND_HALF_EVEN);
	}

	public boolean registrarFactura(Factura factura) {
		return facturaDao.registrar(factura);
	}

	public boolean guardarFacturaTemporal(Factura factura) {
		return facturaOrdenesDao.registrar(factura);
	}

	public void actualizarFacturaTemporal(Factura factura) {
		facturaOrdenesDao.actualizar(factura);
	}

	public void cambiarEstadoOrden(Factura factura, int estado) {
		facturaOrdenesDao.cambiarEstado(factura, estado);
	}

	public List<Factura> obtenerOrdenesPendientes() {
		return facturaOrdenesDao.ordenesPorEmpleadosUsuarios();
	}

	public Factura buscarOrden(int idOrden) {
		return facturaOrdenesDao.buscarPorId(idOrden);
	}

	public List<DetalleFactura> detallesOrdenPendiente(int idOrden) {
		return detallesOrdenDao.detallesFacturaPendiente(idOrden);
	}

	public List<PrecioArticulo> obtenerPreciosSinCosto(int idArticulo) {
		return preciosDao.getPreciosArticuloSinCosto(idArticulo);
	}

	public PrecioArticulo obtenerPrecioArticulo(int idArticulo, int idPrecio) {
		return preciosDao.getPrecioArticulo(idArticulo, idPrecio);
	}

	public boolean eliminarOrden(Factura factura) {
		return facturaOrdenesDao.eliminar(factura);
	}

	public void prepararFactura(Factura factura, Cliente cliente, List<DetalleFactura> detalles,
								boolean esContado) {
		factura.setCliente(cliente);
		factura.setDetalles(detalles);
		factura.setFecha(facturaDao.getFechaSistema());

		if (esContado) {
			factura.setTipoFactura(1);
			factura.setEstadoPago(1);
		} else {
			factura.setTipoFactura(2);
			factura.setEstadoPago(0);
		}
	}

	public void prepararFacturaCredito(Factura factura) {
		factura.setTipoPago(3);
		factura.setCambio(new BigDecimal(0));
		factura.setPago(new BigDecimal(0));
		factura.setTipoFactura(2);
	}

	public boolean validarFactura(List<DetalleFactura> detalles, Cliente cliente) {
		if (detalles == null || detalles.size() <= 1) {
			return false;
		}
		return cliente != null;
	}
}
