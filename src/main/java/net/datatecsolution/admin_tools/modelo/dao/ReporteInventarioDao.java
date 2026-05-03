package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.FilaReporteInventario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReporteInventarioDao extends ModeloDaoBasic {

	public ReporteInventarioDao() {
		super("articulo", "codigo_articulo");
	}

	public FilaReporteInventario consultarPorArticulo(int codigoArticulo, int codigoBodega) {
		return consultarPorArticulo(codigoArticulo, codigoBodega, false);
	}

	public FilaReporteInventario consultarPorArticulo(int codigoArticulo, int codigoBodega, boolean soloComprasVentas) {
		String filtroDescripcion = soloComprasVentas
			? " AND dmk.descripcion IN ('Compra de productos', 'Venta de productos') "
			: "";

		String sql =
			"SELECT a.codigo_articulo AS codigo_articulo, "
			+ "       COALESCE("
			+ "           (SELECT ca.codigo_barra FROM " + DbName + ".codigos_articulos ca "
			+ "             WHERE ca.codigo_articulo = a.codigo_articulo "
			+ "             ORDER BY ca.id_codigo ASC LIMIT 1), "
			+ "           CAST(a.codigo_articulo AS CHAR)"
			+ "       ) AS cod_articulo, "
			+ "       a.articulo AS articulo, "
			+ "       COALESCE(tots.total_ingreso, 0) AS total_ingreso, "
			+ "       COALESCE(tots.total_salida,  0) AS total_salida, "
			+ "       COALESCE(sal.cantidad,       0) AS saldo, "
			+ "       COALESCE(sal.precio_unidad,  0) AS costo "
			+ "FROM " + DbName + ".articulo a "
			+ "LEFT JOIN ("
			+ "    SELECT "
			+ "      SUM(CASE WHEN mk.codigo_tipo_movimiento = 1 THEN mk.cantidad ELSE 0 END) AS total_ingreso, "
			+ "      SUM(CASE WHEN mk.codigo_tipo_movimiento = 2 THEN mk.cantidad ELSE 0 END) AS total_salida "
			+ "    FROM " + DbName + ".articulo_kardex ak "
			+ "    JOIN " + DbName + ".detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex "
			+ "    JOIN " + DbName + ".movimiento_kardex mk ON dmk.codigo_movimiento = mk.codigo_movimiento "
			+ "    WHERE ak.codigo_articulo = ? AND ak.codigo_bodega = ? "
			+ "      AND mk.codigo_tipo_movimiento IN (1, 2) "
			+ filtroDescripcion
			+ ") tots ON 1=1 "
			+ "LEFT JOIN ("
			+ "    SELECT mk.cantidad, mk.precio_unidad "
			+ "    FROM " + DbName + ".articulo_kardex ak "
			+ "    JOIN " + DbName + ".detalle_movimiento_kardex dmk ON ak.codigo_kardex = dmk.codigo_kardex "
			+ "    JOIN " + DbName + ".movimiento_kardex mk ON dmk.codigo_movimiento = mk.codigo_movimiento "
			+ "    WHERE ak.codigo_articulo = ? AND ak.codigo_bodega = ? "
			+ "      AND mk.codigo_tipo_movimiento = 3 "
			+ "    ORDER BY mk.codigo_movimiento DESC "
			+ "    LIMIT 1 "
			+ ") sal ON 1=1 "
			+ "WHERE a.codigo_articulo = ?";

		Connection conn = null;
		ResultSet res = null;
		FilaReporteInventario fila = null;
		try {
			conn = ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(sql);
			int idx = 1;
			psConsultas.setInt(idx++, codigoArticulo);
			psConsultas.setInt(idx++, codigoBodega);
			psConsultas.setInt(idx++, codigoArticulo);
			psConsultas.setInt(idx++, codigoBodega);
			psConsultas.setInt(idx, codigoArticulo);

			res = psConsultas.executeQuery();
			if (res.next()) {
				fila = new FilaReporteInventario();
				fila.setCodigoArticulo(res.getInt("codigo_articulo"));
				fila.setCodArticulo(res.getString("cod_articulo"));
				fila.setArticulo(res.getString("articulo"));
				fila.setTotalIngreso(res.getDouble("total_ingreso"));
				fila.setTotalSalida(res.getDouble("total_salida"));
				fila.setSaldo(res.getDouble("saldo"));
				fila.setCosto(res.getDouble("costo"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (res != null) res.close();
				if (psConsultas != null) psConsultas.close();
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return fila;
	}

	@Override
	public boolean eliminar(Object c) { return false; }

	@Override
	public boolean registrar(Object c) { return false; }

	@Override
	public boolean actualizar(Object c) { return false; }

	@Override
	public java.util.List todos(int limInf, int limSupe) { return null; }

	@Override
	public Object buscarPorId(int id) { return null; }
}
