package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.Conexion;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.view.ViewListaPrecioProgramar;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.util.List;

public class CtlProgramarPrecios implements ActionListener, MouseListener, ChangeListener, WindowListener {
	
	private ViewListaPrecioProgramar view =null;
	private Articulo myArticulo;
	private ArticuloDao myArticuloDao;
	
	//fila selecciona enla lista
	private int filaPulsada;
	
	//pool de conexion
	private Conexion conexion=null;

	public CtlProgramarPrecios(ViewListaPrecioProgramar v,Conexion conn) {
		conexion=conn;
		this.view=v;
		myArticulo=new Articulo();
		myArticuloDao=new ArticuloDao();
		cargarTabla(myArticuloDao.todoArticulos());
		
		view.conectarControlador(this);
		view.setVisible(true);
	}
	
	public void cargarTabla(List<Articulo> articulos){
		//JOptionPane.showMessageDialog(view, articulos);
		
		if(articulos!=null){
			this.view.getModelo().limpiarArticulos();
			for(int c=0;c<articulos.size();c++){
				articulos.get(c).setPrecioVenta(new Double(0));
				this.view.getModelo().agregarArticulo(articulos.get(c));
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		view.setVisible(false);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		case "INSERTAR":
			for(int y=0;y<view.getModelo().getArticulos().size();y++){
				if(view.getModelo().getArticulos().get(y).getPrecioVenta()>0){
					this.myArticuloDao.registraPrecioProgramar(view.getModelo().getArticulos().get(y));
				}
			}
			JOptionPane.showMessageDialog(view, "Se programaron los precios");
			break;
		}
	}

}
