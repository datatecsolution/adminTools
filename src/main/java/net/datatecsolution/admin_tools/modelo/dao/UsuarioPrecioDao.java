package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioPrecioDao extends ModeloDaoBasic {

	public UsuarioPrecioDao() {
		super("usuarios_precios", "id");
	}

	public List<PrecioArticulo> getPreciosPorUsuario(String usuario) {
		List<PrecioArticulo> precios = new ArrayList<PrecioArticulo>();
		Connection con = null;
		ResultSet res = null;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			String sql = "SELECT p.codigo_precio, p.descripcion FROM "
				+ DbNameBase + ".usuarios_precios up JOIN "
				+ DbNameBase + ".precios p ON (up.codigo_precio = p.codigo_precio) "
				+ "WHERE up.usuario = ?";
			psConsultas = con.prepareStatement(sql);
			psConsultas.setString(1, usuario);
			res = psConsultas.executeQuery();
			while (res.next()) {
				PrecioArticulo p = new PrecioArticulo();
				p.setCodigoPrecio(res.getInt("codigo_precio"));
				p.setDecripcion(res.getString("descripcion"));
				precios.add(p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
		} finally {
			cerrar(res, psConsultas, con);
		}
		return precios;
	}

	public boolean asignarPrecio(String usuario, int codigoPrecio) {
		Connection con = null;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			psConsultas = con.prepareStatement(super.getQueryInsert() + " (usuario, codigo_precio) VALUES (?, ?)");
			psConsultas.setString(1, usuario);
			psConsultas.setInt(2, codigoPrecio);
			psConsultas.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			cerrar(null, psConsultas, con);
		}
	}

	public boolean desasignarPrecios(String usuario) {
		Connection con = null;
		try {
			con = ConexionStatic.getPoolConexion().getConnection();
			psConsultas = con.prepareStatement(super.getQueryDelete() + " WHERE usuario = ?");
			psConsultas.setString(1, usuario);
			psConsultas.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			cerrar(null, psConsultas, con);
		}
	}

	private void cerrar(ResultSet res, PreparedStatement ps, Connection con) {
		try {
			if (res != null) res.close();
			if (ps != null) ps.close();
			if (con != null) con.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public List todos(int limInf, int limSupe) { return null; }

	@Override
	public Object buscarPorId(int id) { return null; }

	@Override
	public boolean registrar(Object c) { return false; }

	@Override
	public boolean actualizar(Object c) { return false; }

	@Override
	public boolean eliminar(Object c) { return false; }
}
