package net.datatecsolution.admin_tools.service;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.*;

import java.math.BigDecimal;
import java.util.List;

public class CierreCajaService {

	private final CierreCajaDao cierreCajaDao;
	private final FacturaDao facturaDao;
	private final CierreFacturacionDao cierreFacturacionDao;

	public CierreCajaService() {
		cierreCajaDao = new CierreCajaDao();
		facturaDao = new FacturaDao();
		cierreFacturacionDao = new CierreFacturacionDao();
	}

	public CierreCaja getUltimoCierreUsuario() {
		return cierreCajaDao.getCierreUltimoUser();
	}

	public boolean verificarCierrePendiente(List<Caja> cajas) {
		return facturaDao.verificarCierre(cajas);
	}

	public boolean actualizarCierre(BigDecimal totalEfectivo) {
		return cierreCajaDao.actualizarCierre(totalEfectivo);
	}

	public boolean registrarCierre(CierreCaja nuevo) {
		return cierreCajaDao.registrarCierre(nuevo);
	}

	public CierreCaja buscarPorId(int id) {
		return cierreCajaDao.buscarPorId(id);
	}

	public int getIdUltimoRegistro() {
		return cierreCajaDao.idUltimoRequistro;
	}

	public void cargarCierreFacturas(CierreCaja cierre) {
		cierre.setCierreFacturas(cierreFacturacionDao.buscarIdCierre(cierre.getId()));
	}

	public CierreFacturacion buscarFacturacionPorCajaUsuario(Caja caja, String usuario) {
		return cierreFacturacionDao.buscarPorCajaUsuario(caja, usuario);
	}

	public void getVentasCategorias(Integer noFacturaInicial, Integer noFacturaFinal,
									String usuario, Caja caja, List<VentasCategoria> ventas) {
		facturaDao.getVentasCategorias(noFacturaInicial, noFacturaFinal, usuario, caja, ventas);
	}
}
