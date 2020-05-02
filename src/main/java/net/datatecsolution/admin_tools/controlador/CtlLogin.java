package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Usuario;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.BdConfig;
import net.datatecsolution.admin_tools.view.ViewLogin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlLogin implements ActionListener {
	private ViewLogin view=null;
	private Usuario myUsuario=null;
	private boolean resultado=false;
	private UsuarioDao myUsuarioDao=null;
	
	public CtlLogin(ViewLogin v ){
		view=v;
		myUsuario=new Usuario();
		myUsuarioDao=new UsuarioDao();
		view.conectarControlador(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		case "LOGIN":
				loginBD();
			break;
		case "SALIR":
			System.exit(0);
		break;
		case "BD_CONFIG":
			BdConfig viewBdConfig=new BdConfig(view);
			CtlBdConfig ctlBdConfig=new CtlBdConfig(viewBdConfig);
			
			viewBdConfig.dispose();
			viewBdConfig=null;
			ctlBdConfig=null;
			break;
		
		}
		
	}

	private void loginBD() {
		// TODO Auto-generated method stub
		
		if(ConexionStatic.isDbConnected()){
			try
			{  
				
	            //chekar si el usuario escrbio el nombre de usuario y pw
	            if (view.getTxtUser().getText().length() > 0 && view.getTxtPass().getText().length() > 0 )
	            {
	            	myUsuario.setPwd(view.getTxtPass().getText());
	            	myUsuario.setUser(view.getTxtUser().getText());
	            	resultado=myUsuarioDao.setLogin(myUsuario);
	            	
	            	if(!resultado){//si el resultado el usuario en la bd no existe o el pwd
	            		JOptionPane.showMessageDialog(null, "El nombre de usuario y/o contrasenia no son validos.");
	                    //JOptionPane.showMessageDialog(null, view.getTxtUser().getText()+" " +view.getTxtPass().getText() );
	                    //view.getTxtUser().setText("");    //limpiar campos
	                    view.getTxtPass().setText("");        
	                     
	                    view.getTxtUser().requestFocusInWindow();
	            	}else{
	            		this.view.setVisible(false);
	            		this.view.dispose();
	            		
	            	}
	            	
	                /*/ Si el usuario si fue validado correctamente
	                if( validarUsuario( txtUser.getText(), txtPass.getText() ) )    //enviar datos a validar
	                {
	                    // Codigo para mostrar la ventana principal
	                    setVisible(false);
	                    VentanaPrincipal ventana1 = new VentanaPrincipal();
	                    ventana1.mostrar();
	
	
	                }
	                else
	                {
	                    JOptionPane.showMessageDialog(null, "El nombre de usuario y/o contrasenia no son validos.");
	                    JOptionPane.showMessageDialog(null, view.getTxtUser().getText()+" " +view.getTxtPass().getText() );
	                    view.getTxtUser().setText("");    //limpiar campos
	                    view.getTxtPass().setText("");        
	                     
	                    view.getTxtUser().requestFocusInWindow();
	                }*/
	
	            }
	            else
	            {
	                JOptionPane.showMessageDialog(null, "Debe escribir nombre de usuario y contrasenia.\n" +
	                    "NO puede dejar ningun campo vacio");
	            }
	
	        } catch (Exception e)
	        {
	            e.printStackTrace();
	        }
		}//fin de la comprobacion de la conexion
			
		
		
		
	}

	public boolean login() {
		// TODO Auto-generated method stub
		view.setVisible(true);
		return resultado;
	}

}
