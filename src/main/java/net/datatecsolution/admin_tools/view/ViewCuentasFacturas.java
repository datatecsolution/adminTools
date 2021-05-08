package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCuentasFacturas;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.view.botones.BotonCliente;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.rendes.RtCuentasFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmEmpleado;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmRutasCobro;
import net.datatecsolution.admin_tools.view.tablemodel.TmCuentasFacturas;

import javax.swing.*;
import java.awt.*;

public class ViewCuentasFacturas extends ViewTabla {
	

	protected BotonImprimirSmall btnImprimir;
	protected BotonCliente btnClientes;
	private TmCuentasFacturas modelo;
	private JRadioButton rdbtnCliente;
	private JRadioButton rdbtnRTN;
	private JComboBox<Empleado> cbxEmpleados;
	private CbxTmEmpleado modeloListaEmpleados;

	private JComboBox<RutaCobro> cbxRutas;
	private CbxTmRutasCobro modeloListaRutas;



	private JPopupMenu menuContextual; // permite al usuario seleccionar el color
	private JMenuItem mntmEditarCliente;

	public ViewCuentasFacturas(Window view) {
		super(view,"CXC POR FACTURAS");
		txtPagina.setEnabled(false);
		btnAnterior.setEnabled(false);
		btnSiguiente.setEnabled(false);
		FlowLayout flowLayout = (FlowLayout) panelSuperior.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		btnAgregar.setEnabled(false);

		super.panelEstadoRegistro.setVisible(true);

		menuContextual = new JPopupMenu(); // crea el men� contextual

		//opcion del menu flotante
		mntmEditarCliente = new JMenuItem("Editar cliente");
		menuContextual.add(mntmEditarCliente);


		
		btnEliminar.setToolTipText("Anular Facturas");
		btnEliminar.setVisible(false);
	    
	    btnImprimir = new BotonImprimirSmall();
	    btnImprimir.setEnabled(false);
	    //btnImprimir.setVisible(false);
	    panelAccion.add(btnImprimir);

		btnClientes = new BotonCliente();
		panelAccion.add(btnClientes);
		
		rdbtnCliente = new JRadioButton("Cliente");
		panelOpcioneBusqueda.add(rdbtnCliente);
		grupoOpciones.add(rdbtnCliente);

		rdbtnRTN =new JRadioButton("RTN",false);
		panelOpcioneBusqueda.add(rdbtnRTN);
		grupoOpciones.add(rdbtnRTN);
	  
		rdbtnFecha.setVisible(false);

		rdbtnInactivo.setVisible(false);

		rdbtnId.setText("No Cuenta");

		modeloListaEmpleados=new CbxTmEmpleado();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaEmpleados.agregar(new Empleado());

		cbxEmpleados = new JComboBox<Empleado>(modeloListaEmpleados);
		panelOpcioneBusqueda.add(cbxEmpleados);

		modeloListaRutas=new CbxTmRutasCobro();
		modeloListaRutas.agregar(new RutaCobro());

		cbxRutas=new JComboBox<RutaCobro>(modeloListaRutas);
		panelOpcioneBusqueda.add(cbxRutas);



		panelPaginacion.setVisible(false);
		panelTotalReg.setVisible(true);
		

		
		
	    
	    //tabla y sus componentes
		modelo=new TmCuentasFacturas();
		
		tabla.setModel(modelo);
		RtCuentasFacturas renderizador = new RtCuentasFacturas();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(10);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(10);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(300);	//en la tabla
		tabla.getColumnModel().getColumn(3).setPreferredWidth(50);	//
		tabla.getColumnModel().getColumn(4).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(5).setPreferredWidth(150);	//
		tabla.getColumnModel().getColumn(6).setPreferredWidth(40);

		setPreferredSize(new Dimension(1200,680));

		this.setSize(1200,680);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

		
		
		
		
	}
	
public void conectarControlador(CtlCuentasFacturas c){
		
		rdbtnTodos.addActionListener(c);
		rdbtnTodos.setActionCommand("ESCRIBIR");
		rdbtnTodos.addKeyListener(c);
		
		rdbtnId.addActionListener(c);
		rdbtnId.setActionCommand("ESCRIBIR");
		rdbtnId.addKeyListener(c);
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("ESCRIBIR");
		rdbtnFecha.addKeyListener(c);

		
		
		rdbtnCliente.addActionListener(c);
		rdbtnCliente.setActionCommand("ESCRIBIR");
		rdbtnCliente.addKeyListener(c);

		rdbtnRTN.addActionListener(c);
		rdbtnRTN.setActionCommand("ESCRIBIR");
		rdbtnRTN.addKeyListener(c);



		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		txtBuscar.addKeyListener(c);
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		btnBuscar.addKeyListener(c);


		btnClientes.addActionListener(c);
		btnClientes.setActionCommand("PAGOS");
		btnClientes.addKeyListener(c);
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 btnAgregar.addKeyListener(c);
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 btnEliminar.addKeyListener(c);
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
		 btnImprimir.addKeyListener(c);
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 txtBuscar.addKeyListener(c);
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 btnSiguiente.addKeyListener(c);
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 btnAnterior.addKeyListener(c);

		rdbtnActivos.addActionListener(c);
		rdbtnActivos.setActionCommand("REG_ACTIVO");

		rdbtnInactivo.addActionListener(c);
		rdbtnInactivo.setActionCommand("REG_INACTIVO");

		rdbtnTodosEstado.addActionListener(c);
		rdbtnTodosEstado.setActionCommand("REG_TODOS");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.addKeyListener(c);

		cbxEmpleados.addActionListener(c);
		cbxEmpleados.setActionCommand("CAMBIOCOMBOBOX");
		cbxEmpleados.addKeyListener(c);

		cbxRutas.addActionListener(c);
		cbxRutas.setActionCommand("CAMBIOCOMBOBOXRUTA");
		cbxRutas.addKeyListener(c);

		mntmEditarCliente.addActionListener(c);
		mntmEditarCliente.setActionCommand("EDITAR_CLIENTE");

		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public JPopupMenu getMenuContextual() {
		return menuContextual;
	}

	public void setMenuContextual(JPopupMenu menuContextual) {
		this.menuContextual = menuContextual;
	}
	public JComboBox<Empleado> getCbxEmpleados() {
		return cbxEmpleados;
	}
	public JComboBox<RutaCobro> getCbxRutas() {
		return cbxRutas;
	}

	public void setCbxEmpleados(JComboBox<Empleado> cbxEmpleados) {
		this.cbxEmpleados = cbxEmpleados;
	}
	public void setCbxRutas(JComboBox<RutaCobro> cbxRuta) {
		this.cbxRutas = cbxRuta;
	}
	public CbxTmEmpleado getModeloListaEmpleados() {
		return modeloListaEmpleados;
	}
	public CbxTmRutasCobro getModeloListaRutas() {
		return modeloListaRutas;
	}

	public void setModeloListaEmpleados(CbxTmEmpleado modeloListaEmpleados) {
		this.modeloListaEmpleados = modeloListaEmpleados;
	}
	public JRadioButton getRdbtnCliente() {
		return rdbtnCliente;
	}
	public void setRdbtnCliente(JRadioButton rdbtnCliente) {
		this.rdbtnCliente = rdbtnCliente;
	}

	
	public TmCuentasFacturas getModelo(){
		return modelo;
	}
	
	public BotonImprimirSmall getBtnImprimir(){
		return btnImprimir;
	}
	public JRadioButton getRdbtnFecha(){
		return rdbtnFecha;
	}
	public JRadioButton getRdbtnTodos(){
		return rdbtnTodos;
		
	}

	public JRadioButton getRdbtnRTN() {
		return rdbtnRTN;
	}

	public void setRdbtnRTN(JRadioButton rdbtnRTN) {
		this.rdbtnRTN = rdbtnRTN;
	}

	

}
