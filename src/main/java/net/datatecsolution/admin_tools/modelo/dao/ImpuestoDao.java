package net.datatecsolution.admin_tools.modelo.dao;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Impuesto;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class ImpuestoDao extends ModeloDaoBasic {
	
	
	
	public ImpuestoDao(){
		super("impuesto", "codigo_impuesto");
	}
	
	
/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< selecciona de la Bd todas las MArcas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	public Vector<Impuesto> todoImpuesto(){
		
		//List<Impuesto> impuestos=new ArrayList<Impuesto>();
		Vector<Impuesto> impuestos=new Vector<Impuesto>();
		ResultSet res=null;
		Connection conn=null;
		//DefaultComboBoxModel modelImpuesto = new DefaultComboBoxModel();
		boolean existe=false;
		try {
			conn=ConexionStatic.getPoolConexion().getConnection();
			psConsultas = conn.prepareStatement(super.getQuerySelect());
			
			res = psConsultas.executeQuery();
			
			while(res.next()){
				Impuesto unaImpuesto=new Impuesto();
				existe=true;
				unaImpuesto.setId(Integer.parseInt(res.getString("codigo_impuesto")));
				unaImpuesto.setPorcentaje(res.getString("porcentaje"));
				impuestos.add(unaImpuesto);
				
				//modelImpuesto.addElement(new Impuesto((String)res.getString("codigo_impuesto"), (String)res.getString("porcentaje")));
			 }
			//res.close();
			//conexion.desconectar();
					
					
			} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Error, no se conecto");
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
				return impuestos;
			}
			else return null;
			
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
