package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Banco;
import net.datatecsolution.admin_tools.modelo.CuentaBanco;
import net.datatecsolution.admin_tools.modelo.dao.BancosDao;
import net.datatecsolution.admin_tools.modelo.dao.CuentaBancosDao;
import net.datatecsolution.admin_tools.view.ViewCuentaBanco;
import net.datatecsolution.admin_tools.view.ViewFiltroSaldoBanco;
import net.datatecsolution.admin_tools.view.ViewMovimientoBanco;

import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class CtlCuentasBanco implements ActionListener,MouseListener, WindowListener, ItemListener {
	public ViewCuentaBanco view;
	
	
	private CuentaBanco myCuentaBanco;
	private CuentaBancosDao myCuentaBancoDao;
	
	//fila selecciona enla lista
	private int filaPulsada;


	private BancosDao myBancoDao;
	
	
	public CtlCuentasBanco(ViewCuentaBanco v){
		//se estable el pool de conexiones del inicio y para su utilizacion
		
		this.view=v;
		myCuentaBanco=new CuentaBanco();
		myCuentaBancoDao=new CuentaBancosDao();
		
		
		
		view.conectarControlador(this);
		
		myBancoDao=new BancosDao();
		
		cargarComboBox(myBancoDao.getCuentas());
		
	
		
		myCuentaBancoDao.setMyBanco(view.getModeloCuentasBancos().getBanco(view.getCbxBanco().getSelectedIndex()));
		
		cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
		
		view.setVisible(true);
		//this.view.setVisible(true);
		
	}
	
	private void cargarComboBox(Vector<Banco> bancos){
			
			if(bancos!=null){
				//se obtiene la lista de los formas de pago y se le pasa al modelo de la lista
				this.view.getModeloCuentasBancos().setLista(bancos);
				
				
				//se remueve la lista por defecto
				this.view.getCbxBanco().removeAllItems();
			
				this.view.getCbxBanco().setSelectedIndex(0);
			}
		
		}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		//se recoge el comando programado en el boton que se hizo click
		String comando=e.getActionCommand();
		
		//se establece la bodega para todas las busqueda
		myCuentaBancoDao.setMyBanco(view.getModeloCuentasBancos().getBanco(view.getCbxBanco().getSelectedIndex()));
		
		
		switch(comando){
		
		
		
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "BUSCAR":
			view.getModelo().setPaginacion();
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){  
				//myArticulo=myArticuloDao.buscarArticulo(Integer.parseInt(this.view.getTxtBuscar().getText()));
				myCuentaBanco=(CuentaBanco) myCuentaBancoDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
				this.view.getModelo().limpiar();
				if(myCuentaBanco!=null){												
					this.view.getModelo().agregar(myCuentaBanco);
				}
			} 
			
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				this.view.getTxtBuscar().setText("");
				}
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		
		case "INSERTAR":
			
			ViewMovimientoBanco viewMovimientoBanco=new ViewMovimientoBanco(view);
			CtlMovimientoBanco ctlMovimientoBanco=new CtlMovimientoBanco(viewMovimientoBanco);
			viewMovimientoBanco.dispose();
			viewMovimientoBanco=null;
			ctlMovimientoBanco=null;
			
			myCuentaBancoDao.setMyBanco(view.getModeloCuentasBancos().getBanco(view.getCbxBanco().getSelectedIndex()));
			
			cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
			
			
			break;
		
			
			
		case "NEXT":
			//se establece los datos de la nueva pagina
			view.getModelo().netPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				
				
				cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				
			}
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			//se establece los datos de la pagina anterior
			view.getModelo().lastPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  

				cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
				
				
			}
			
			//se establece el numero de pagina en la view
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
			
		case "REPORTE":
			
			ViewFiltroSaldoBanco viewFiltroSaldoBanco=new ViewFiltroSaldoBanco(view);
			CtlFiltroRepBanco ctlFiltroRepBanco=new CtlFiltroRepBanco(viewFiltroSaldoBanco);
			
			viewFiltroSaldoBanco.dispose();
			viewFiltroSaldoBanco=null;
			ctlFiltroRepBanco=null;
			break;
			
				
			}
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
        	//Se recoge el id de la fila marcada
          //  int identificador= (int)this.view.getModelo().getValueAt(filaPulsada, 0);
            
          //se consigue el proveedore de la fila seleccionada
            myCuentaBanco=this.view.getModelo().getCuenta(filaPulsada);
        
            
        	//si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		
        		//se consigue el articulo de la fila donde se hizo doble click
	        	myCuentaBanco=this.view.getModelo().getCuenta(filaPulsada);
        		//myArticulo=this.view.getModelo().getArticulo(filaPulsada);//se consigue el Marca de la fila seleccionada
	           
	      
			
				
				
				
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
	
	
	public void cargarTabla(List<CuentaBanco> cuentas){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModelo().limpiar();
		
		if(cuentas!=null){
			for(int c=0;c<cuentas.size();c++){
				this.view.getModelo().agregar(cuentas.get(c));
				
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
		//this.myArticulorDao.close();
		this.view.setVisible(false);
		//this.conexion.desconectar();
		
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
	public void itemStateChanged(ItemEvent e1) {
		// TODO Auto-generated method stub
		
		if(e1.getStateChange() == ItemEvent.SELECTED){
			myCuentaBancoDao.setMyBanco(view.getModeloCuentasBancos().getBanco(view.getCbxBanco().getSelectedIndex()));
			
			cargarTabla(myCuentaBancoDao.todos(view.getModelo().getCanItemPag(),view.getModelo().getLimiteSuperior()));
         }
		
	}

}
