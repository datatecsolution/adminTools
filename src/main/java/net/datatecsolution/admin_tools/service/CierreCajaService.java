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
		CierreCaja ultimoCierreUser = cierreCajaDao.getCierreUltimoUser();
		String usuario = ConexionStatic.getUsuarioLogin().getUser();
		boolean resultado = false;

		for (int x = 0; x < cajas.size(); x++) {
			Caja caja = cajas.get(x);

			CierreFacturacion registroFacturasCaja = cierreFacturacionDao.buscarPorCajaUsuario(
					ConexionStatic.getUsuarioLogin().getCajas().get(x),
					usuario,
					ultimoCierreUser.getId());

			if (registroFacturasCaja != null) {
				resultado = facturaDao.verificarCierre(caja, registroFacturasCaja);
				if (resultado) {
					break;
				}
			} else {
				CierreFacturacion newFacturasCaja = new CierreFacturacion();
				if (facturaDao.verificarCierre(caja, newFacturasCaja)) {
					newFacturasCaja.setCodigoCierre(ultimoCierreUser.getId());
					newFacturasCaja.setUsuario(usuario);
					newFacturasCaja.setCaja(caja);
					//US-149: la fila reparada continua el ultimo turno cerrado;
					//con el 0 por defecto el cierre sumaria toda la historia
					Integer finalPrevio = cierreFacturacionDao.ultimoFinalPorCajaUsuario(
							caja, usuario, ultimoCierreUser.getId());
					newFacturasCaja.setNoFacturaInicio(finalPrevio != null ? finalPrevio + 1 : 1);
					cierreFacturacionDao.registrar(newFacturasCaja);
				}
			}
		}

		return resultado;
	}

	public boolean actualizarCierre(BigDecimal totalEfectivo) {
		return cierreCajaDao.actualizarCierre(totalEfectivo);
	}

	public boolean registrarCierre(CierreCaja nuevo) {
		return cierreCajaDao.registrarCierre(nuevo);
	}

	public boolean registrarCierreActual() {
		return cierreCajaDao.registrar(new Object());
	}

	public CierreCaja buscarPorId(int id) {
		return cierreCajaDao.buscarPorId(id);
	}

	public List<CierreCaja> todos(int limInf, int limSupe) {
		return cierreCajaDao.todos(limInf, limSupe);
	}

	public List<CierreCaja> buscarPorFecha(String desde, String hasta, int limitInferior, int canItemPag) {
		return cierreCajaDao.buscarPorFecha(desde, hasta, limitInferior, canItemPag);
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

	/**
	 * US-149: numero de factura inicial para la apertura del turno en una caja.
	 * Propaga ConsultaCierreException si alguna consulta falla: la apertura
	 * debe abortarse, nunca continuar con un rango inventado.
	 */
	public int calcularFacturaInicialApertura(Caja caja, String usuario) {
		CierreFacturacion previa = cierreFacturacionDao.buscarPorCajaUsuario(caja, usuario);
		if (AperturaTurno.tieneFinalUsable(previa)) {
			return previa.getNoFacturaFinal() + 1;
		}
		Factura ultima = facturaDao.getUltimaFacturaUser(usuario, caja);
		return AperturaTurno.calcularFacturaInicial(previa, ultima);
	}

	public void getVentasCategorias(Integer noFacturaInicial, Integer noFacturaFinal,
									String usuario, Caja caja, List<VentasCategoria> ventas) {
		facturaDao.getVentasCategorias(noFacturaInicial, noFacturaFinal, usuario, caja, ventas);
	}
}
