package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlClienteBuscar;
import net.datatecsolution.admin_tools.controlador.CtlClienteLista;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.view.botones.BotonCuenta;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmEmpleado;
import net.datatecsolution.admin_tools.view.tablemodel.TablaModeloCliente;

import javax.swing.*;
import java.awt.*;

public class ViewListaClientes extends ViewTabla {
	
	protected JButton btnLimpiar;
	
	
	
	private JRadioButton rdbtnNombre;
	private JRadioButton rdbtnRtn;

	
	private TablaModeloCliente modelo;

	private BotonReporte btnReporte;
	private BotonCuenta btnCuenta;
	private JComboBox<Empleado> cbxEmpleados;
	private CbxTmEmpleado modeloListaEmpleados;
	
	
	
	public ViewListaClientes(Window view) {
		
		super(view,"Clientes");

        
        btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaClientes.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
        btnReporte=new BotonReporte();
        panelAccion.add(btnReporte);
        
        btnCuenta=new BotonCuenta();
        panelAccion.add(btnCuenta);
        
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		rdbtnRtn = new JRadioButton("RTN",false);
		panelOpcioneBusqueda.add(rdbtnRtn);
		grupoOpciones.add(rdbtnRtn);
		
		modeloListaEmpleados=new CbxTmEmpleado();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaEmpleados.agregar(new Empleado());
		
		cbxEmpleados = new JComboBox<Empleado>(modeloListaEmpleados);
		panelOpcioneBusqueda.add(cbxEmpleados);
		
		 //tabla y sus componentes
		modelo=new TablaModeloCliente();
		
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(3).setPreferredWidth(10);	//
		
		
		
	}
	
	public TablaModeloCliente getModelo(){
		return modelo;
	}
	
	
	public JRadioButton getRdbtnNombre(){
		return rdbtnNombre;
	}
	public JRadioButton getRdbtnRtn(){
		return  rdbtnRtn;
		
	}
	
	public void conectarControladorBuscar(CtlClienteBuscar c){
		
		rdbtnTodos.addKeyListener(c);
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("NUEVO");
		btnAgregar.addKeyListener(c);
		
		this.btnEliminar.addKeyListener(c);
		this.btnCuenta.addKeyListener(c);
		this.btnLimpiar.addKeyListener(c);
		this.btnReporte.addKeyListener(c);
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		rdbtnId.addKeyListener(c);
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		rdbtnNombre.addKeyListener(c);
		
		rdbtnRtn.addActionListener(c);
		rdbtnRtn.setActionCommand("ESCRIBIR");
		rdbtnRtn.addKeyListener(c);
		
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
		
		rdbtnRtn.addActionListener(c);
		rdbtnRtn.setActionCommand("ESCRIBIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		btnSiguiente.addActionListener(c);
		btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 btnReporte.addActionListener(c);
		 btnReporte.setActionCommand("CUENTASCLIENTES");
		 
		 btnCuenta.addActionListener(c);
		 btnCuenta.setActionCommand("CUENTACLIENTE");
		 
		
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
