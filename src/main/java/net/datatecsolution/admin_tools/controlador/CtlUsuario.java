package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewCrearUsuario;
import net.datatecsolution.admin_tools.view.ViewListaCajas;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class CtlUsuario extends MouseAdapter implements ActionListener {
	private ViewCrearUsuario view=null;
	private Usuario myUsuario=null;
	private UsuarioDao myDao=null;
	private boolean resultaOperacion=false;
	
	public CtlUsuario(ViewCrearUsuario v){
		view=v;
		
		myUsuario=new Usuario();
		myDao=new UsuarioDao();
		
		view.conectarCtl(this);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "AGREGARCAJA":
				ViewListaCajas vListaCajas=new ViewListaCajas(view);
				CtlCajasBuscar cListaCajaBuscar=new CtlCajasBuscar(vListaCajas);
				
				boolean resul=cListaCajaBuscar.buscarCaja();
				
				
				
				if(resul){
					boolean verificar=view.getModeloListaCajas().verificarDuplicado(cListaCajaBuscar.getMyCaja());
					//se verifica que no este la caja en la lista ya
					if(verificar){
						JOptionPane.showMessageDialog(view, "La caja ya esta asignada al usuario","Error",JOptionPane.ERROR_MESSAGE);
					}else{
						view.getModeloListaCajas().addCaja(cListaCajaBuscar.getMyCaja());
					}
				}
				
			break;
		case "GUARDAR":
			if(validar()){
				setUser();
				guardarUsuario();
			}
			break;
		case "CANCELAR":
			view.setVisible(false);
			break;
		case "ACTUALIZAR":
			
			if(validar()){
				setUser();
				actualizarUsuario();
			}
			
			break;
			
		case "ELIMINARCODIGO":
			
			//se obtiene el index del codigo selecionado
			int indexCodigoSelecionado=this.view.getlCajas().getSelectedIndex();
			
			Caja cajaSelect=this.view.getModeloListaCajas().getCaja(indexCodigoSelecionado);
			
			//se pregunta si en verdad se quiere borrar el codigo de bgarra
			int confirmacion=JOptionPane.showConfirmDialog(view, "Desea eliminar ["+cajaSelect+"] del usuario?");
			
			// si se confirma la eliminacion se procede a eliminar
			if(confirmacion==0){
				
				//se eliminar el codigo en la view list y se coloca el eliminado en una lista especial para eliminar de la bd
				this.view.getModeloListaCajas().eliminarCaja(indexCodigoSelecionado);
				
				
			}
			break;
		case "SETDEFAULT":
			//se obtiene el index del codigo selecionado
			int indexCodigoSelecionado2=this.view.getlCajas().getSelectedIndex();
			
			Caja cajaSelect2=this.view.getModeloListaCajas().getCaja(indexCodigoSelecionado2);
			
			//se pregunta si en verdad se quiere borrar el codigo de bgarra
			int confirmacion2=JOptionPane.showConfirmDialog(view, "Desea establecer default la caja ["+cajaSelect2+"] del usuario?");
			
			// si se confirma la eliminacion se procede a eliminar
			if(confirmacion2==0){
				
				//se eliminar el codigo en la view list y se coloca el eliminado en una lista especial para eliminar de la bd
				this.view.getModeloListaCajas().setCajaDefault(indexCodigoSelecionado2);
				
				
			}
			
			break;
		}
		
	}
	
	private void actualizarUsuario() {
		// TODO Auto-generated method stub
		if(myDao.actualizar(myUsuario)){
			JOptionPane.showMessageDialog(view, "El Usuario se actualizo correctamente.");
			this.resultaOperacion=true;
			view.setVisible(false);
		}else{
			JOptionPane.showMessageDialog(view, "Ocurrio un problema para actualizar el usuario");
		}
	}

	private void guardarUsuario(){
		if(myDao.registrar(myUsuario)){
			JOptionPane.showMessageDialog(view, "El Usuario se guardo correctamente.","Exito",JOptionPane.INFORMATION_MESSAGE);
			this.resultaOperacion=true;
			view.setVisible(false);
		}else{
			JOptionPane.showMessageDialog(view, "Ocurrio un problema para guardar el usuario","Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	private void setUser() {
		// TODO Auto-generated method stub
		myUsuario.setUser(view.getTxtUser().getText());
		
		myUsuario.setApellido(view.getTxtApellido().getText());
		
		myUsuario.setNombre(view.getTxtNombre().getText());
		
		myUsuario.setPwd(view.getPwd().getText());
		
		myUsuario.setCajas(view.getModeloListaCajas().getCajas());
		
		//JOptionPane.showMessageDialog(null, view.getModeloListaCajas().getCajas().size());
		if(view.getRdbtnAdministrador().isSelected()){
			myUsuario.setTipoPermiso(1);
			myUsuario.setPermiso("Administrador");
		}
		if(view.getRdbtnCajero().isSelected()){
			myUsuario.setTipoPermiso(2);
			myUsuario.setPermiso("Cajero");
		}
		
		
	}

	private boolean validar() {
		// TODO Auto-generated method stub
		
		
		String jpf1Text=Arrays.toString(view.getPwd().getPassword());//get the char array of password and convert to string represenation
		String jpf2Text=Arrays.toString(view.getRePwd().getPassword());
		boolean resul=false;
		if(view.getTxtUser().getText().trim().length()==0){
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getTxtUser().requestFocusInWindow();
		}else
		if(view.getTxtNombre().getText().trim().length()==0){
			JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
			view.getTxtNombre().requestFocusInWindow();
		}else
			if(view.getTxtApellido().getText().trim().length()==0){
				JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
				view.getTxtApellido().requestFocusInWindow();
			}else
				if(view.getPwd().getPassword().length==0){
					JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
					view.getPwd().requestFocusInWindow();
				}else
					if(view.getRePwd().getPassword().length==0){
						JOptionPane.showMessageDialog(view, "Debe rellenar todos los campos");
						view.getRePwd().requestFocusInWindow();
					}else
						if(!jpf1Text.equals(jpf2Text)){
							JOptionPane.showMessageDialog(view, "Password no son iguales!!!");
							view.getPwd().requestFocusInWindow();
						}else
							resul=true;
		
		return resul;
	}

	public boolean agregarUsuario() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return resultaOperacion;
	}

	public Usuario getUsuario() {
		// TODO Auto-generated method stub
		return myUsuario;
	}
	public void loadUsuario(){
		view.getTxtUser().setText(myUsuario.getUser());
		view.getTxtNombre().setText(myUsuario.getNombre());
		view.getTxtApellido().setText(myUsuario.getApellido());
		
		if(myUsuario.getTipoPermiso()==1){
			view.getRdbtnAdministrador().setSelected(true);
		}
		if(myUsuario.getTipoPermiso()==2){
			view.getRdbtnCajero().setSelected(true);
		}
		
		//si tiene cajas asignas se muestran en la view
		if(myUsuario.getCajas()!=null){
			view.getModeloListaCajas().setCajas(myUsuario.getCajas());
		}
	}
	
	
	public boolean actualizarUsuario(Usuario user){
		myUsuario=user;
		myUsuario.setUserOld(user.getUser());
		
		//JOptionPane.showMessageDialog(view, myUsuario);
		
		view.getBtnActualizar().setVisible(true);
		view.getBtnGuardar().setVisible(false);
		loadUsuario();
		view.setVisible(true);
		return resultaOperacion;
	}
	
	// maneja el evento de oprimir el boton del raton
		public void mousePressed( MouseEvent evento )
		{
			check(evento);
			checkForTriggerEvent( evento ); // comprueba el desencadenador
		} // fin del metodo mousePressed
		
		// maneja el evento de liberacion del boton del raton
		public void mouseReleased( MouseEvent evento )
		{
			check(evento);
			checkForTriggerEvent( evento ); // comprueba el desencadenador
		} // fin del metodo mouseReleased
		
		// determina si el evento debe desencadenar el menu contextual
		private void checkForTriggerEvent( MouseEvent evento )
		{
			if ( evento.isPopupTrigger() )
				this.view.getMenuContextual().show(evento.getComponent(), evento.getX(), evento.getY() );
		} // fin del motodo checkForTriggerEvent
		
		public void check(MouseEvent e)
		{ 
			if (e.isPopupTrigger()) { //if the event shows the menu 
				this.view.getlCajas().setSelectedIndex(this.view.getlCajas().locationToIndex(e.getPoint())); //select the item 
				//menuContextual.show(listCodigos, e.getX(), e.getY()); //and show the menu 
			} 
			
			
		}

}
