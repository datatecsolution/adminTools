package net.datatecsolution.admin_tools.modelo;

import java.util.ArrayList;
import java.util.List;

public class Usuario extends Persona {
	private int codigo;
	private String user="";
	private String userOld="";
	private String permiso="";
	private String pwd="";
	private int tipo_permiso;
	private List<Caja> cajasAsignadas=new ArrayList<Caja>();
	private ConfigUserFacturacion config=new ConfigUserFacturacion();
	
	public void setCodigo(int c){
		codigo=c;
	}
	public int getCodigo(){
		return codigo;
	}
	public void setTipoPermiso(int tp){
		tipo_permiso=tp;
	}
	public int getTipoPermiso(){
		return tipo_permiso;
	}
	
	public void setUser(String u){
		user=u;
	}
	public String getUser(){
		return user;
	}
	public void setUserOld(String u){
		userOld=u;
	}
	public String getUserOld(){
		return userOld;
	}
	
	public void setPermiso(String p){
		permiso=p;
	}
	public String getPermiso(){
		return permiso;
	}
	
	
	public void setPwd(String p){
		pwd=p;
	}
	public String getPwd(){
		return pwd;
	}
	
	@Override
	public String toString(){
		return "Codigo: "
				+this.codigo+ ","
						+ "User: "
						+ this.user+", "
							+ super.toString()+ ","
								+ "Tipo de permiso: "
								+ this.tipo_permiso+ ", "
										+ "Permiso: "
										+ this.permiso+", "
										+"user old: "
										+this.userOld;
						
	}
	public List<Caja> getCajas() {
		return cajasAsignadas;
	}
	public void setCajasEmpty(){
		cajasAsignadas=new ArrayList<Caja>();
	}
	public void setCajas(List<Caja> cajas) {
		//this.cajasAsignadas.clear();
		this.cajasAsignadas = cajas;
	}
	public Caja getCajaActiva(){
		Caja una=new Caja();
		boolean existe=false;
		
		if(getCajas()!=null)
		{
			
			for(int x=0;x<getCajas().size();x++){
				
				if(getCajas().get(x).isActiva())
				{
					existe=true;
					una=getCajas().get(x);
				}
			}
		}
		
		if(existe)
			return una;
		else
			return null;
	}
	public Caja nextCaja(){
		
		
		
		int indexNext=0;
		
		if(getCajas()!=null)
		{
			//se consigue el index de la caja por defecto
			for(int x=0;x<getCajas().size();x++){
				
				if(getCajas().get(x).isActiva())
				{
					indexNext=x;
				//	JOptionPane.showMessageDialog(null, indexNext);
				}
			}
			//JOptionPane.showMessageDialog(null, getCajas().size());
			
			//se comprueba que se puede pasar a la siguiente caja
			if((indexNext+1)<getCajas().size()){//si estamos sino estamos en el top de la lista seguimos avanzando
				
				getCajas().get(indexNext).setActiva(false);//se desactiva la anterio
				indexNext=indexNext+1;						//se pasa a la siguiente
				getCajas().get(indexNext).setActiva(true);//se activa la siguiente
				
			}else{//si estamos en el top de la lista 
				
				getCajas().get(indexNext).setActiva(false);//colocamos el top desactivida
				indexNext=0;//retrocedemos a la primera 
				getCajas().get(indexNext).setActiva(true);//activamos la primera
				
			}
			
		}
		
		
		return getCajas().get(indexNext);
		
		
	}
	public Caja setCajaActica(Integer codigoCaja){
		
		Integer codigoActica=-1;
		for(int x=0;x<getCajas().size();x++){
			
			if(getCajas().get(x).getCodigo()==codigoCaja){
				
				getCajas().get(x).setActiva(true);
				codigoActica=x;
				
			}else{
				
				getCajas().get(x).setActiva(false);
				
			}
			
		}
		return getCajas().get(codigoActica);
		
	}
	
	public ConfigUserFacturacion getConfig() {
		return config;
	}
	
	public void setConfig(ConfigUserFacturacion config) {
		this.config = config;
	}
			

}
