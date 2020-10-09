package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Departamento;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class DepartamentoDao extends ModeloDaoBasic {
	

	public DepartamentoDao(){
		super(" bodega ","codigo_bodega");
	}
	
	
	
//	*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< todos los departamentos *////////////////
	public Vector<Departamento> todosExecto(int id){
		Vector<Departamento> depats=new Vector<Departamento>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where bodega.codigo_bodega<>?");
			
			psConsultas.setInt(1, id);
			
			
			
			res=psConsultas.executeQuery();
			while(res.next()){
				Departamento unDept=new Departamento();
				existe=true;
				unDept.setId(res.getInt("codigo_bodega"));
				unDept.setDescripcion(res.getString("descripcion_bodega"));
				depats.add(unDept);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res!=null)res.close();
				if(psConsultas!=null)psConsultas.close();
				if(conn!=null)conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
			} // fin de catch
		} // fin de finally
		
		if(existe)
			return depats;
		else
			return null;
			
	}
	
//	*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< todos los departamentos *////////////////
	public Vector<Departamento> todos(){
		Vector<Departamento> depats=new Vector<Departamento>();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect());


			System.out.println(psConsultas);
			res=psConsultas.executeQuery();
			while(res.next()){
				Departamento unDept=new Departamento();
				existe=true;
				unDept.setId(res.getInt("codigo_bodega"));
				unDept.setDescripcion(res.getString("descripcion_bodega"));
				depats.add(unDept);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res!=null)res.close();
				if(psConsultas!=null)psConsultas.close();
				if(conn!=null)conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
			} // fin de catch
		} // fin de finally
		
		if(existe)
			return depats;
		else
			return null;
			
	}
	
	public Departamento buscarBodega(int id){
		Departamento myBodega=new Departamento();
		ResultSet res=null;
		Connection conn=null;
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas=conn.prepareStatement(super.getQuerySelect()+" where codigo_bodega=?");
			
			
			psConsultas.setInt(1, id);
			res=psConsultas.executeQuery();
			while(res.next()){
				existe=true;
				myBodega.setId(res.getInt("codigo_bodega"));
				myBodega.setDescripcion(res.getString("descripcion_bodega"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try{
				if(res!=null)res.close();
				if(psConsultas!=null)psConsultas.close();
				if(conn!=null)conn.close();
			} // fin de try
			catch ( SQLException excepcionSql )
			{
				excepcionSql.printStackTrace();
			} // fin de catch
		} // fin de finally
		
		if(existe)
			return myBodega;
		else
			return null;
			
	}



	@Override
	public boolean eliminar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean registrar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public boolean actualizar(Object c) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public List todos(int limInf, int limSupe) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object buscarPorId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

}
