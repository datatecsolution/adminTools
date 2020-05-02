package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.modelo.DatosFacturacion;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DatosFacturacionDao;
import net.datatecsolution.admin_tools.view.ViewCrearDatosFacturacion;
import net.datatecsolution.admin_tools.view.ViewListaDatosFacturacion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class CtlDatosFacturacionLista implements ActionListener, MouseListener {
	
	private ViewListaDatosFacturacion view;
	
	private DatosFacturacionDao myDatosFdao;
	private CajaDao cajaDao;
	//fila selecciona enla lista
	private int filaPulsada;
	public CtlDatosFacturacionLista(ViewListaDatosFacturacion v){
		view=v;
		
		view.conectarControlador(this);
		cajaDao=new CajaDao();
		myDatosFdao=new DatosFacturacionDao();
		cargarComboBox();
		cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
		view.setVisible(true);
	}
	
	public void cargarTabla(List<DatosFacturacion> datosF){
		//JOptionPane.showMessageDialog(view, articulos);
		this.view.getModelo().limpiar();
		if(datosF!=null){
			for(int c=0;c<datosF.size();c++){
				this.view.getModelo().agregar(datosF.get(c));
			}
		}
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCajas().setLista(this.cajaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbxCajas().removeAllItems();
	
		this.view.getCbxCajas().setSelectedIndex(0);
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger que fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){
        	
            
            //si fue doble click mostrar modificar
        	if (e.getClickCount() == 2) {
        		//se consigue el item de la fila donde se hizo doble click
        		DatosFacturacion myDato=this.view.getModelo().getDatoF(view.getTabla().getSelectedRow());
        		
        		ViewCrearDatosFacturacion vCrearNuevo=new ViewCrearDatosFacturacion(view);
    			CtlDatosFacturacion ctlCrearNuevo=new CtlDatosFacturacion(vCrearNuevo);
    			
    			boolean resulActualizar=ctlCrearNuevo.actualizarDatos(myDato);
    			
    			
    			if(resulActualizar){
    				//se consegue la caja en donde se agrego los nuevos datos de facturacion
    				Caja c=ctlCrearNuevo.getDatosF().getCaja();
    				
    				//se busca el item en el comboBox para seleccionar
    				int cajaGuarda=view.getModeloListaCajas().buscarCaja(c);
    				
    				//se selecciona la caja en el comboBox que se guardo para que cambie los datos de facturacion
    				view.getCbxCajas().setSelectedIndex(cajaGuarda);
    			}
    			
    			vCrearNuevo.dispose();
    			vCrearNuevo=null;
    			ctlCrearNuevo=null;
        		
        		
        	}
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
		
		//se recoge el comando programado en el boton que se hizo click
		String comando=e.getActionCommand();
		

		
		
		switch(comando){
		
		case "CAMBIOCOMBOBOX":
			//cuando cambia la seleccion del comboBox caja se cambia los datos para esa caja
			cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
				
		break;
		case "ESCRIBIR":
			view.setTamanioVentana(1);
			break;
		case "BUSCAR":
			//si se seleciono el boton ID
			if(this.view.getRdbtnId().isSelected()){  
				DatosFacturacion unDato= myDatosFdao.buscarPorId(Integer.parseInt(view.getTxtBuscar().getText()),view.getCbxCajas().getSelectedItem());
				if(unDato!=null){
					view.getModelo().limpiar();
					view.getModelo().agregar(unDato);
					
				}else{
					JOptionPane.showMessageDialog(null, "No se encontro el registro","Error",JOptionPane.ERROR_MESSAGE);
				}
			} 
			
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
				this.view.getTxtBuscar().setText("");
				}
			break;
		
		case "INSERTAR":
			ViewCrearDatosFacturacion vCrearNuevo=new ViewCrearDatosFacturacion(view);
			CtlDatosFacturacion ctlCrearNuevo=new CtlDatosFacturacion(vCrearNuevo);
			
			boolean resulAgregar=ctlCrearNuevo.agregarDatos();
			 
			if(resulAgregar){
				//se consegue la caja en donde se agrego los nuevos datos de facturacion
				Caja c=ctlCrearNuevo.getDatosF().getCaja();
				
				//se busca el item en el comboBox para seleccionar
				int cajaGuarda=view.getModeloListaCajas().buscarCaja(c);
				
				//se selecciona la caja en el comboBox que se guardo para que cambie los datos de facturacion
				view.getCbxCajas().setSelectedIndex(cajaGuarda);
			}
			vCrearNuevo.dispose();
			vCrearNuevo=null;
			ctlCrearNuevo=null;
			
			
			break;
		case "ELIMINAR":
			
			if(view.getTabla().getSelectedRowCount()!=0){
				//se consigue el item de la fila donde se hizo doble click
	    		DatosFacturacion myDato=this.view.getModelo().getDatoF(view.getTabla().getSelectedRow());
	    		
	    		//se pregunta si en verdad se quiere borrar el codigo de bgarra
				int confirmacion=JOptionPane.showConfirmDialog(view, "Desea eliminar "+myDato.toString()+" ?");
				
				// si se confirma la eliminacion se procede a eliminar
				if(confirmacion==0){
					//se verifica que se puede eliminiar en la base de datos
					if(myDatosFdao.verificarFacturacionEliminacion(myDato)==false){
						//se manda a eliminar en la base de datos
						if(myDatosFdao.eliminar(myDato)){
							//cuando cambia la seleccion del comboBox caja se cambia los datos para esa caja
							cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
						}
					}else{
						JOptionPane.showMessageDialog(view, "No se puede eleminar los datos de facturacion porque hay facturas utilizandolo.","Error",JOptionPane.ERROR_MESSAGE);
					}
				}
			}else{
				JOptionPane.showMessageDialog(view, "Debe seleccionar la fila que desea eliminar.","Error",JOptionPane.ERROR_MESSAGE);
			}
			
			break;
			
		case "LIMPIAR":
			
			break;
			
		case "NEXT":
			view.getModelo().netPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
		case "LAST":
			view.getModelo().lastPag();
			
			if(this.view.getRdbtnTodos().isSelected()){  
				cargarTabla(myDatosFdao.todos(view.getModelo().getCanItemPag(), view.getModelo().getLimiteSuperior(),view.getCbxCajas().getSelectedItem()));
			}
			
			view.getTxtPagina().setText(""+view.getModelo().getNoPagina());
			break;
			
		
			
		
			
				
			}
		
		
	}

}
