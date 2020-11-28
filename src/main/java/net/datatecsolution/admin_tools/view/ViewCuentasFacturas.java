package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCuentasFacturas;
import net.datatecsolution.admin_tools.view.botones.BotonCliente;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.TmCuentasFacturas;

import javax.swing.*;
import java.awt.*;

public class ViewCuentasFacturas extends ViewTabla {
	

	protected BotonImprimirSmall btnImprimir;
	 protected BotonCliente btnClientes;
	
	
	
	private TmCuentasFacturas modelo;
	private JRadioButton rdbtnCliente;

	

	public JRadioButton getRdbtnCliente() {
		return rdbtnCliente;
	}

	public void setRdbtnCliente(JRadioButton rdbtnCliente) {
		this.rdbtnCliente = rdbtnCliente;
	}

	public ViewCuentasFacturas(Window view) {
		super(view,"CXC POR FACTURAS");
		txtPagina.setEnabled(false);
		btnAnterior.setEnabled(false);
		btnSiguiente.setEnabled(false);
		FlowLayout flowLayout = (FlowLayout) panelSuperior.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		btnAgregar.setEnabled(false);


		

	
	
		
		btnEliminar.setToolTipText("Anular Facturas");
	    
	    btnImprimir = new BotonImprimirSmall();
	    btnImprimir.setEnabled(false);
	    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
	    panelAccion.add(btnImprimir);

		btnClientes = new BotonCliente();
		panelAccion.add(btnClientes);
		
		rdbtnCliente = new JRadioButton("Cliente");
		panelOpcioneBusqueda.add(rdbtnCliente);
		grupoOpciones.add(rdbtnCliente);
	  
		rdbtnFecha.setVisible(true);
		

		
		
	    
	    //tabla y sus componentes
		modelo=new TmCuentasFacturas();
		
		tabla.setModel(modelo);
		RenderizadorTablaFacturas renderizador = new RenderizadorTablaFacturas();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(60);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(80);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(250);	//en la tabla
		//tabla.getColumnModel().getColumn(3).setPreferredWidth(100);	//
		//tabla.getColumnModel().getColumn(4).setPreferredWidth(100);	//
		//tabla.getColumnModel().getColumn(5).setPreferredWidth(100);	//

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
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.addKeyListener(c);
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

	

}
