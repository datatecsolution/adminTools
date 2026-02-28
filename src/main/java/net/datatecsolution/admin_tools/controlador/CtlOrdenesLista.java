package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaOrdenVentaDao;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewListaClientes;
import net.datatecsolution.admin_tools.view.ViewListaOrdenes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.List;

public class CtlOrdenesLista implements ActionListener, MouseListener {
	private ViewListaOrdenes view;

	private FacturaOrdenVentaDao ordenDao =null;
	private Factura myOrden =null;
	private EmpleadoDao empleadoDao=null;

	//fila selecciona enla lista
	private int filaPulsada;
	//String[] estados = {"Activo", "Modificado", "Facturado", "Eliminado"};

	Estado[] estados = {
			new Estado(1, "Activo"),
			new Estado(2, "Modificado"),
			new Estado(3, "Facturado"),
			new Estado(4, "Eliminado")
	};

	public CtlOrdenesLista(ViewListaOrdenes v){
		view=v;
		view.conectarControlador(this);
		empleadoDao=new EmpleadoDao ();

		view.getBtnAgregar().setEnabled(true);
		view.getBtnEliminar().setEnabled(true);
		//view.getRdbtnNombre().setSelected(true);
		ordenDao =new FacturaOrdenVentaDao();
		
		cargarComboBox();
		view.setVisible(true);
		cargarTabla(ordenDao.buscarPorNombreCliente(this.view.getTxtBuscar().getText(),view.getModelo().getLimiteSuperior(),view.getModelo().getCanItemPag()));

	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaEmpleados().setLista(this.empleadoDao.todoEmpleadosVendedores());

		//si es la primera vez que entra a la vista se seleciona el primier elemento
		if(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda().getNombre().isEmpty())
			this.view.getCbxEmpleados().setSelectedIndex(0);
		
		
		//se remueve la lista por defecto
		this.view.getCbxEmpleados().removeAllItems();
	
		//
		int vendedor=view.getModeloListaEmpleados().buscarEmpleado(ConexionStatic.getUsuarioLogin().getConfig().getVendedorEnBusqueda());
		this.view.getCbxEmpleados().setSelectedIndex(vendedor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando = e.getActionCommand();
		switch (comando) {
			case "ESCRIBIR":
				view.setTamanioVentana(1);
				break;

			case "CAMBIOCOMBOBOX":
				//JOptionPane.showMessageDialog(view, "Cambio el vendedor");

				Empleado miEmpleado = (Empleado) view.getCbxEmpleados().getSelectedItem();

				if (miEmpleado != null) {
					ConexionStatic.getUsuarioLogin().getConfig().setVendedorEnBusqueda(miEmpleado);
				}


				break;
			case "ID":
				JOptionPane.showMessageDialog(view, "Click en busquda por id");
				break;
			case "BUSCAR":


				//si se seleciono el boton ID
				view.getModelo().setPaginacion();

				if (this.view.getRdbtnTodos().isSelected()) {
					//JOptionPane.showMessageDialog(view,"Vamos a buscar");
					cargarTabla(ordenDao.buscarPorVendedorCliente("", view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));
					this.view.getTxtBuscar().setText("");
				}
				if (this.view.getRdbtnId().isSelected()) {
					myOrden = ordenDao.buscarPorId(Integer.parseInt(this.view.getTxtBuscar().getText()));
					if (myOrden != null) {
						this.view.getModelo().limpiarFacturas();
						this.view.getModelo().agregarFactura(myOrden);
					} else {
						JOptionPane.showMessageDialog(view, "No se encuentro el registro", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}

				if (this.view.getRdbtnNombre().isSelected()) { //si esta selecionado la busqueda por nombre

					cargarTabla(ordenDao.buscarPorVendedorCliente(this.view.getTxtBuscar().getText(), view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));

				}
				view.getTxtPagina().setText("" + view.getModelo().getNoPagina());
				break;
			case "NUEVO":
				break;

			case "NEXT":
				view.getModelo().netPag();
				if (this.view.getRdbtnTodos().isSelected()) {
					cargarTabla(ordenDao.buscarPorVendedorCliente("", view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));
				}
				if (this.view.getRdbtnNombre().isSelected()) { //si esta selecionado la busqueda por nombre

					cargarTabla(ordenDao.buscarPorVendedorCliente(this.view.getTxtBuscar().getText(), view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));

				}
				view.getTxtPagina().setText("" + view.getModelo().getNoPagina());
				break;
			case "LAST":
				view.getModelo().lastPag();
				if (this.view.getRdbtnTodos().isSelected()) {
					cargarTabla(ordenDao.buscarPorVendedorCliente("", view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));

				}
				if (this.view.getRdbtnNombre().isSelected()) { //si esta selecionado la busqueda por nombre

					cargarTabla(ordenDao.buscarPorVendedorCliente(this.view.getTxtBuscar().getText(), view.getModelo().getLimiteSuperior(), view.getModelo().getCanItemPag()));

				}
				view.getTxtPagina().setText("" + view.getModelo().getNoPagina());
				break;
			case "IMPRIMIR":

				//Recoger qu� fila se ha pulsadao en la tabla
				filaPulsada = this.view.getTabla().getSelectedRow();
				//JOptionPane.showMessageDialog(view, "click en la tabla"+filaPulsada);

				//si seleccion una fila
				if (filaPulsada >= 0) {
					int idFacturaTemporal = this.view.getModelo().getFactura(filaPulsada).getIdFactura();

					//se imprime un tike de salida para prueba
					try {
						AbstractJasperReports.createReportOrdenCarta(ConexionStatic.getPoolConexion().getConnection(), idFacturaTemporal);

						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);


					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}

				}
				break;

			case "CAMBIAR_ESTADO":
				//Recoger qu� fila se ha pulsadao en la tabla
				filaPulsada = this.view.getTabla().getSelectedRow();
				//JOptionPane.showMessageDialog(view, "click en la tabla"+filaPulsada);

				//si seleccion una fila
				if (filaPulsada >= 0) {
					//se captura la factura de la linea seleccioada
					Factura facturaTemporal = this.view.getModelo().getFactura(filaPulsada);
					//se excluye el index del estado actual de la factura
					int indiceExcluir = Integer.parseInt( this.view.getModelo().getFactura(filaPulsada).getEstado());


					//se creal el modelo de datos con los estados para el comboBox
					DefaultComboBoxModel<Estado> modelo = new DefaultComboBoxModel<>();
					for (Estado estado : estados) {
						if (estado.getIndice() != indiceExcluir) {
							modelo.addElement(estado);
						}
					}
					//se crea el JComboBox con los estados
					JComboBox<Estado> comboBox = new JComboBox<>(modelo);

					//Se muestra el JComboBox en un JOptionPane para seleccionar
					int opcion = JOptionPane.showConfirmDialog(
							view,
							comboBox,
							"Estado para orden # "+facturaTemporal.getIdFactura(),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE
					);

					//si en el JOptionPane se presiono el boton Ok se cambia el estado en la base de datos de la factura
					if (opcion == JOptionPane.OK_OPTION) {
						Estado seleccionado = (Estado) comboBox.getSelectedItem();
						ordenDao.cambiarEstado(facturaTemporal,seleccionado.indice);

						view.getTxtBuscar().setText(facturaTemporal.getIdFactura()+"");
						view.getRdbtnId().setSelected(true);


						view.getBtnBuscar().doClick();
					}
				}


				break;
		}
	}
	private static int obtenerIndiceOriginal(String seleccion, String[] estados, int indiceExcluir) {
		for (int i = 0; i < estados.length; i++) {
			if (i != indiceExcluir && estados[i].equals(seleccion)) {
				return i;
			}
		}
		return -1; // En caso de error
	}
	public void cargarTabla(List<Factura> ordenes){

		this.view.getModelo().limpiarFacturas();
		if(ordenes!=null){
			for(int c=0;c<ordenes.size();c++){
				this.view.getModelo().agregarFactura(ordenes.get(c));
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//Recoger qu� fila se ha pulsadao en la tabla
        filaPulsada = this.view.getTabla().getSelectedRow();
        
        //si seleccion una fila
        if(filaPulsada>=0){

        	//Se recoge el id de la fila marcada
            int identificador= (int)this.view.getModelo().getValueAt(filaPulsada, 0);

//          //se consigue el proveedore de la fila seleccionada
//            myCliente=this.view.getModelo().getCliente(filaPulsada);
//
//
//        	//si fue doble click mostrar modificar
//        	if (e.getClickCount() == 2) {
//
//        		myCliente=this.view.getModelo().getCliente(filaPulsada);
//        		//myArticulo=this.view.getModelo().getArticulo(filaPulsada);//se onsigue el Marca de la fila seleccionada
//
//	        	//crea la ventana para ingresar un nuevo proveedor
//				ViewCrearCliente viewCliente= new ViewCrearCliente();
//
//				//se crea el controlador de la ventana y se le pasa la view
//				CtlCliente ctlActulizarCliente=new CtlCliente(viewCliente);
//
//
//
//
//				//se llama del metodo actualizar marca para que se muestre la ventanda y procesa la modificacion
//				boolean resultado=ctlActulizarCliente.actualizarCliente(myCliente);
//
//				//se proceso el resultado de modificar la marca
//				if(resultado){
//					this.view.getModelo().cambiarCliente(filaPulsada, ctlActulizarCliente.getClienteGuardado());//se cambia en la vista
//					this.view.getModelo().fireTableDataChanged();//se refrescan los cambios
//					this.view.getTabla().getSelectionModel().setSelectionInterval(filaPulsada,filaPulsada);//se seleciona lo cambiado
//				}
//
//				ctlActulizarCliente=null;
//				viewCliente=null;
//
//	        }//fin del if del doble click
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

	class Estado {
		private int indice;
		private String nombre;

		public Estado(int indice, String nombre) {
			this.indice = indice;
			this.nombre = nombre;
		}

		public int getIndice() {
			return indice;
		}

		public String getNombre() {
			return nombre;
		}

		@Override
		public String toString() {
			return nombre; // Para que JComboBox muestre solo el nombre
		}
	}

}
