package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.RutaCobro;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RutaCobroDao extends ModeloDaoBasic{
		private int idRegistrado;

		public RutaCobroDao() {
			// TODO Auto-generated constructor stub
			super("rutas_cobro","codigo_ruta");
		}

		/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Metodo para seleccionar todos los articulos>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
		public Vector<RutaCobro> todoRutas(){



			Connection con = null;



			Vector<RutaCobro> rutas=new Vector<RutaCobro>();

			ResultSet res=null;

			boolean existe=false;
			try {
				con = ConexionStatic.getPoolConexion().getConnection();

				psConsultas=con.prepareStatement(super.getQuerySelect());
				//psConsultas.setInt(1,1);
				//System.out.println(psConsultas);
				res = super.psConsultas.executeQuery();
				while(res.next()){
					RutaCobro unRuta=new RutaCobro();
					existe=true;
					unRuta.setCodigo(res.getInt("codigo_ruta"));
					unRuta.setDescripcion(res.getString("descripcion"));
					unRuta.setObser(res.getString("observaciones"));


					rutas.add(unRuta);
				}

			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
			}
			finally
			{
				try{

					if(res != null) res.close();
					if(super.psConsultas != null)psConsultas.close();
					if(con != null) con.close();


				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally


			if (existe) {
				return rutas;
			}
			else return null;

		}

		/**
		 * Metodo para buscar un empleado en la base de datos
		 * @param id
		 * @return Empleado encontrado
		 */
		@Override
		public RutaCobro buscarPorId(int id) {
			// TODO Auto-generated method stub

			RutaCobro myRuta=new RutaCobro();


			ResultSet res=null;
			boolean existe=false;
			Connection con = null;
			try {
				con = ConexionStatic.getPoolConexion().getConnection();

				psConsultas=con.prepareStatement(super.getQuerySearch("codigo_ruta", "="));
				psConsultas.setInt(1, id);
				psConsultas.setInt(2, 0);
				psConsultas.setInt(3, 1);

				//System.out.println(psConsultas);
				res = psConsultas.executeQuery();
				while(res.next()){

					existe=true;
					myRuta.setCodigo(res.getInt("codigo_ruta"));
					myRuta.setDescripcion(res.getString("descripcion"));
					myRuta.setObser(res.getString("observaciones"));



				}

			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			finally
			{
				try{

					if(res != null) res.close();
					if(psConsultas != null)psConsultas.close();
					if(con != null) con.close();


				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally


			if (existe) {
				return myRuta;
			}
			else return null;
		}
		@Override
		public boolean registrar(Object c) {
			// TODO Auto-generated method stub

			RutaCobro myRuta=(RutaCobro) c;
			int resultado=0;
			ResultSet rs=null;
			Connection con = null;

			try
			{
				con = ConexionStatic.getPoolConexion().getConnection();

				psConsultas=con.prepareStatement( super.getQueryInsert()+" (descripcion,observaciones) VALUES (?,?)",java.sql.Statement.RETURN_GENERATED_KEYS);

				psConsultas.setString(1, myRuta.getDescripcion());
				psConsultas.setString(2, myRuta.getObser());
				resultado=psConsultas.executeUpdate();

				rs=psConsultas.getGeneratedKeys(); //obtengo las ultimas llaves generadas
				while(rs.next()){
					this.setIdRegistrado(rs.getInt(1));
				}


				return true;
			}catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				return false;
			}
			finally
			{
				try{
					if(rs!=null)rs.close();
					if(psConsultas != null)psConsultas.close();
					if(con != null) con.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally
		}//fin de registrar

		@Override
		public List<RutaCobro> todos(int limInf,int limSupe) {
			List<RutaCobro> rutas =new ArrayList<RutaCobro>();

			ResultSet res=null;

			Connection conn=null;

			boolean existe=false;

			try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				//psConsultas=conn.prepareStatement(super.getQuerySelect()+ "join (select codigo_empleado from "+super.DbName+"."+super.tableName+" where codigo_empleado<=((SELECT max(codigo_empleado) from "+ super.DbName+"."+super.tableName+")-?)  ORDER BY codigo_empleado DESC LIMIT ? ) empleados2 on(empleados2.codigo_empleado="+super.tableName+".codigo_empleado ) ORDER BY "+super.tableName+".codigo_empleado DESC");
				psConsultas=conn.prepareStatement(super.getQueryRecord());
				psConsultas.setInt(1, limSupe);
				psConsultas.setInt(2, limInf);
				res = psConsultas.executeQuery();
				while(res.next()){
					existe=true;
					RutaCobro un=new RutaCobro();
					un.setCodigo(res.getInt("codigo_ruta"));
					un.setDescripcion(res.getString("descripcion"));
					un.setObser(res.getString("observaciones"));
					rutas.add(un);


				}
			}catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
			}
			finally
			{
				try{
					if(res != null) res.close();
					if(psConsultas != null)psConsultas.close();
					if(conn != null) conn.close();

				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally


			if (existe) {
				return rutas;
			}
			else return null;
		}
		@Override
		public boolean actualizar(Object c) {
			RutaCobro myRuta=(RutaCobro) c;
			int resultado;
			Connection conn=null;

			try {
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQueryUpdate()+" SET descripcion = ?, observaciones = ? WHERE codigo_ruta = ?");
				psConsultas.setString( 1, myRuta.getDescripcion() );
				psConsultas.setString( 2, myRuta.getObser() );
				psConsultas.setInt( 3, myRuta.getCodigo() );
				resultado=psConsultas.executeUpdate();
				return true;
			}catch (SQLException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				return false;
			}
			finally
			{
				try{

					if(psConsultas != null)psConsultas.close();
					if(conn != null) conn.close();
				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally

		}

		/**
		 * Metodo para buscar los empleados por el nombre
		 * @param busqueda
		 * @param limite de paginacion
		 * @param cantidad de item por pagina
		 * @return lista de empleados encontrados
		 */
		public List<RutaCobro> porDescripcion(String busqueda,int limitInferio, int canItemPag) {
			List<RutaCobro> rutas =new ArrayList<RutaCobro>();

			ResultSet res=null;

			Connection conn=null;

			boolean existe=false;

			try{
				conn=ConexionStatic.getPoolConexion().getConnection();
				psConsultas=conn.prepareStatement(super.getQuerySearch("descripcion", "like"));
				psConsultas.setString(1, "%" + busqueda + "%");
				psConsultas.setInt(2, limitInferio);
				psConsultas.setInt(3, canItemPag);
				res = psConsultas.executeQuery();
				while(res.next()){
					existe=true;
					RutaCobro un=new RutaCobro();
					un.setCodigo(res.getInt("codigo_ruta"));
					un.setDescripcion(res.getString("descripcion"));
					un.setObser(res.getString("observaciones"));
					rutas.add(un);


				}
			}catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),"Error en la base de datos",JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
			}
			finally
			{
				try{
					if(res != null) res.close();
					if(psConsultas != null)psConsultas.close();
					if(conn != null) conn.close();

				} // fin de try
				catch ( SQLException excepcionSql )
				{
					excepcionSql.printStackTrace();
					//conexion.desconectar();
				} // fin de catch
			} // fin de finally


			if (existe) {
				return rutas;
			}
			else return null;
		}

		/**
		 * Metodo para buscar los empleados por el apellido
		 * @param busqueda
		 * @param limite de paginacion
		 * @param cantidad de item por pagina
		 * @return lista de paginas
		 */
		public List<RutaCobro> porObser(String busqueda,int limitInferio, int canItemPag) {
			List<RutaCobro> rutas = new ArrayList<RutaCobro>();

			ResultSet res = null;

			Connection conn = null;

			boolean existe = false;

			try {
				conn = ConexionStatic.getPoolConexion().getConnection();
				psConsultas = conn.prepareStatement(super.getQuerySearch("observaciones", "like"));
				psConsultas.setString(1, "%" + busqueda + "%");
				psConsultas.setInt(2, limitInferio);
				psConsultas.setInt(3, canItemPag);
				res = psConsultas.executeQuery();
				while (res.next()) {
					existe = true;
					RutaCobro un=new RutaCobro();
					un.setCodigo(res.getInt("codigo_ruta"));
					un.setDescripcion(res.getString("descripcion"));
					un.setObser(res.getString("observaciones"));
					rutas.add(un);

				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
				System.out.println(e);
			} finally {
				try {
					if (res != null)
						res.close();
					if (psConsultas != null)
						psConsultas.close();
					if (conn != null)
						conn.close();

				} // fin de try
				catch (SQLException excepcionSql) {
					excepcionSql.printStackTrace();
					// conexion.desconectar();
				} // fin de catch
			} // fin de finally

			if (existe) {
				return rutas;
			} else
				return null;
		}
		public void setIdRegistrado(int i){
			idRegistrado=i;
		}
		public int getIdRegistrado(){
			return idRegistrado;
		}

		@Override
		public boolean eliminar(Object c) {
			// TODO Auto-generated method stub
			return false;
		}


}
