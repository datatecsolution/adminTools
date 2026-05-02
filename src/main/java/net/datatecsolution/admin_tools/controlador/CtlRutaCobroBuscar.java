package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.modelo.dao.RutaCobroDao;
import net.datatecsolution.admin_tools.view.ViewListaRutasCobro;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlRutaCobroBuscar implements ActionListener, MouseListener, WindowListener, KeyListener {

	private final ViewListaRutasCobro view;
	private final RutaCobroDao myDao;
	private int filaPulsada;
	private RutaCobro myRuta;
	private boolean resultado=false;

	public CtlRutaCobroBuscar(ViewListaRutasCobro v) {
		view=v;

		myDao=new RutaCobroDao();

		view.conectarCtlBuscar(this);

		myRuta=new RutaCobro();

		cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
	}

	public boolean buscar() {
		view.getRdbtnDescripcion().setSelected(true);
		view.getTxtBuscar().requestFocusInWindow();
		view.setVisible(true);
		return resultado;
	}

	private void cargarTabla(List<RutaCobro> rutas) {
		view.getModelo().limpiar();
		if(rutas!=null){
			for(int x=0;x<rutas.size();x++)
				view.getModelo().agregar(rutas.get(x));
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getComponent()==this.view.getTxtBuscar()&&view.getTxtBuscar().getText().trim().length()!=0){

			if(this.view.getRdbtnDescripcion().isSelected()){

				this.filaPulsada=this.view.getTabla().getSelectedRow();

				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					filaPulsada++;
					this.view.getTabla().setRowSelectionInterval(0, filaPulsada);
					myRuta=view.getModelo().getRuta(filaPulsada);

				}else if(e.getKeyCode()==KeyEvent.VK_UP){
					filaPulsada--;
					this.view.getTabla().setRowSelectionInterval(0, filaPulsada);
					myRuta=view.getModelo().getRuta(filaPulsada);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			resultado=false;
			myRuta=null;
			view.setVisible(false);
		}

		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			if(filaPulsada>0){
				myRuta=this.view.getModelo().getRuta(filaPulsada);
				this.resultado=true;
				view.setVisible(false);
			}
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		this.myRuta=null;
		view.setVisible(false);
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		filaPulsada = this.view.getTabla().getSelectedRow();
		if (e.getClickCount() == 2){
			this.myRuta=this.view.getModelo().getRuta(filaPulsada);
			resultado=true;
			this.view.setVisible(false);
			this.view.dispose();
		}
	}

	public RutaCobro getRutaSelected(){
		return myRuta;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando=e.getActionCommand();
		switch(comando){

			case "ESCRIBIR":
				view.setTamanioVentana(1);
				break;

			case "BUSCAR":
				view.getModelo().setPaginacion();

				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
					view.getTxtBuscar().setText("");
				}else if(view.getRdbtnDescripcion().isSelected()){
					cargarTabla(myDao.porDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}else if(view.getRdbtnObservacion().isSelected()){
					cargarTabla(myDao.porObser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}else if(view.getRdbtnId().isSelected()){
					myRuta=myDao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText()));
					if(myRuta!=null){
						view.getModelo().limpiar();
						view.getModelo().agregar(myRuta);
					}else{
						JOptionPane.showMessageDialog(view, "No se encontro la ruta.");
					}
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;

			case "NEXT":
				view.getModelo().netPag();
				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				if(view.getRdbtnDescripcion().isSelected()){
					cargarTabla(myDao.porDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				if(view.getRdbtnObservacion().isSelected()){
					cargarTabla(myDao.porObser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;

			case "LAST":
				view.getModelo().lastPag();
				if(view.getRdbtnTodos().isSelected()){
					cargarTabla(myDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				}
				if(view.getRdbtnDescripcion().isSelected()){
					cargarTabla(myDao.porDescripcion(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				if(view.getRdbtnObservacion().isSelected()){
					cargarTabla(myDao.porObser(view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));
				}
				view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
				break;
		}
	}

}
