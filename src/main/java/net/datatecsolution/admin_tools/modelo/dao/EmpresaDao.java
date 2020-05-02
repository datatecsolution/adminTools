package net.datatecsolution.admin_tools.modelo.dao;

import java.util.List;


public class EmpresaDao extends ModeloDaoBasic {
	
	
	

	public EmpresaDao() {
		// TODO Auto-generated constructor stub
		super("datos_empresa","codigo_rango");
		
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
