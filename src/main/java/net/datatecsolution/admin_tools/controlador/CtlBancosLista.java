package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.view.ViewCrearBanco;
import net.datatecsolution.admin_tools.view.ViewListaCuentaBancos;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class CtlBancosLista implements ActionListener, MouseListener {
	
	
	private ViewListaCuentaBancos view=null;
	private Banco myCuenta=null;
	private BancosDao myDao=null;
	private int filaPulsada=-1;
	
	public CtlBancosLista(ViewListaCuentaBancos v){
		view=v;
		view.conectarCtl(this);

		myDao=new BancosDao();
		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
	}
	
	public void cargarTabla(List<Banco> cuentas){
		//JOptionPane.showMessageDialog(view, " "+facturas.size());
		this.view.getModelo().limpiar();
		
		if(cuentas!=null){
			for(int c=0;c<cuentas.size();c++){
				this.view.getModelo().agregar(cuentas.get(c));
				
			}
		}else{
			JOptionPane.showMessageDialog(view, "No se encontro ninguna cotizacion. Escriba otra busqueda", "Error al buscar", JOptionPane.ERROR_MESSAGE);
			view.getTxtBuscar().requestFocusInWindow();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
			
		
		 //si seleccion una fila
       if(filaPulsada>=0){
       	
       	
       	//se consigue la cuenta de la fila seleccionada
       this.myCuenta=view.getModelo().getCuenta(filaPulsada);
       	
       	//si fue doble click mostrar modificar
       	if (e.getClickCount() == 2) {
       		
       		ViewCrearBanco vCuenta=new ViewCrearBanco(view);
			CtlBanco cCuenta=new CtlBanco(vCuenta);
			
			boolean resultado=cCuenta.actualizarCuenta(myCuenta);
			if(resultado){
				cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			}
       		
       		
       	}//fin del if del doble click
       	
       	
       }
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		
			case "INSERTAR":
				ViewCrearBanco vCuenta=new ViewCrearBanco(view);
				CtlBanco cCuenta=new CtlBanco(vCuenta);
				
				boolean resultado=cCuenta.agregarCuenta();
				if(resultado){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				break;
		
		}
		
		
	}

}
