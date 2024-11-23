package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlClienteBuscar;
import net.datatecsolution.admin_tools.controlador.CtlClienteLista;
import net.datatecsolution.admin_tools.controlador.CtlOrdenesBuscar;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.view.botones.BotonCuenta;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmEmpleado;
import net.datatecsolution.admin_tools.view.tablemodel.TablaModeloCliente;
import net.datatecsolution.admin_tools.view.tablemodel.TmOrdenes;

import javax.swing.*;
import java.awt.*;

public class ViewListaOrdenes extends ViewTabla {

	//protected JButton btnLimpiar;



	private JRadioButton rdbtnNombre;

	private JButton btnImprimir;
//	private JRadioButton rdbtnRtn;


	private TmOrdenes modelo;

	//private BotonReporte btnReporte;
	//private BotonCuenta btnCuenta;
	private JComboBox<Empleado> cbxEmpleados;
	private CbxTmEmpleado modeloListaEmpleados;



	public ViewListaOrdenes(Window view) {
		
		super(view,"Ordenes");

        btnImprimir=new BotonImprimirSmall();
        panelAccion.add(btnImprimir);
        
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);

		
		modeloListaEmpleados=new CbxTmEmpleado();//comentar para poder mostrar en forma de diseno la ventana
		//modeloListaEmpleados.agregar(new Empleado());
		
		cbxEmpleados = new JComboBox<Empleado>(modeloListaEmpleados);
		panelOpcioneBusqueda.add(cbxEmpleados);
		
		 //tabla y sus componentes
		modelo=new TmOrdenes();
		
		tabla.setModel(modelo);
		RenderizadorTablaFacturas renderizador = new RenderizadorTablaFacturas();
		tabla.setDefaultRenderer(String.class, renderizador);

		tabla.getColumnModel().getColumn(0).setPreferredWidth(60);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(90);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(70);	//en la tabla
		tabla.getColumnModel().getColumn(3).setPreferredWidth(280);	//

		
		
		
	}
	
	public TmOrdenes getModelo(){
		return modelo;
	}
	
	
	public JRadioButton getRdbtnNombre(){
		return rdbtnNombre;
	}
//	public JRadioButton getRdbtnRtn(){
//		return  rdbtnRtn;
//
//	}
	
	public void conectarControladorBuscar(CtlOrdenesBuscar c){

		rdbtnId.setVisible(false);
		
		rdbtnTodos.addKeyListener(c);
		rdbtnTodos.setActionCommand("BUSCAR");
		rdbtnTodos.addKeyListener(c);
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("NUEVO");
		btnAgregar.addKeyListener(c);
		
		this.btnEliminar.addKeyListener(c);
		this.btnImprimir.addKeyListener(c);
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		rdbtnId.addKeyListener(c);
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		rdbtnNombre.addKeyListener(c);

		btnImprimir.addActionListener(c);
		btnImprimir.setActionCommand("IMPRIMIR");
		btnImprimir.addKeyListener(c);
		
//		rdbtnRtn.addActionListener(c);
//		rdbtnRtn.setActionCommand("ESCRIBIR");
//		rdbtnRtn.addKeyListener(c);
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		btnBuscar.addKeyListener(c);
		
		
		
		btnSiguiente.addActionListener(c);
		btnSiguiente.setActionCommand("NEXT");
		btnSiguiente.addKeyListener(c);
		
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.addKeyListener(c);
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		txtBuscar.addKeyListener(c);
		
		cbxEmpleados.addActionListener(c);
		cbxEmpleados.setActionCommand("CAMBIOCOMBOBOX");
		cbxEmpleados.addKeyListener(c);
		
	}
	public void conectarControlador(CtlClienteLista c){
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("NUEVO");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		cbxEmpleados.addActionListener(c);
		cbxEmpleados.setActionCommand("CAMBIOCOMBOBOX");
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
//		rdbtnRtn.addActionListener(c);
//		rdbtnRtn.setActionCommand("ESCRIBIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		btnSiguiente.addActionListener(c);
		btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 btnImprimir.addActionListener(c);
		 //btnCuenta.setActionCommand("CUENTACLIENTE");
		 
		
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * @return the cbxEmpleados
	 */
	public JComboBox<Empleado> getCbxEmpleados() {
		return cbxEmpleados;
	}

	/**
	 * @return the modeloListaEmpleados
	 */
	public CbxTmEmpleado getModeloListaEmpleados() {
		return modeloListaEmpleados;
	}

}
