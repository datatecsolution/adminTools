package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlPagoLista;
import net.datatecsolution.admin_tools.view.botones.BotonCobrador;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.TmPagos;

import javax.swing.*;
import java.awt.*;

public class ViewListaPagos extends ViewTabla {
	
	
	protected BotonCobrador btnCobrador;
	private TmPagos modelo;
	private BotonReporte btnReporte;



	private JRadioButton rdbtnRef;



	public ViewListaPagos(Window view) {
		super(view,"Pagos de clientes");
		
		
		
	
	    
	    btnCobrador = new BotonCobrador();
	    //btnImprimir.setEnabled(false);
	    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
	    panelAccion.add(btnCobrador);
	    //panelAccion.setVisible(false);
	    
	    btnReporte=new BotonReporte();
	    panelAccion.add(btnReporte);
	    
	 
		
	    rdbtnFecha.setVisible(true);

		rdbtnRef=new JRadioButton("Referecia",false);
		panelOpcioneBusqueda.add(rdbtnRef);
		grupoOpciones.add(rdbtnRef);
		
		
				
		
        //tabla y sus componentes
		modelo=new TmPagos();
		tabla.setModel(modelo);
		RenderizadorTablaFacturas renderizador = new RenderizadorTablaFacturas();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(20);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(20);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(250);	//en la tabla
		tabla.getColumnModel().getColumn(3).setPreferredWidth(70);	//
		
		
		
		getContentPane().add(panelSuperior, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		//setSize(900,565);
	
	}
	
	
	public TmPagos getModelo(){
		return modelo;
	}
	
	public BotonCobrador getBtnCobrador(){
		return btnCobrador;
	}

	public JRadioButton getRdbtnRef() {
		return rdbtnRef;
	}

	public void setRdbtnRef(JRadioButton rdbtnRef) {
		this.rdbtnRef = rdbtnRef;
	}

	
	public void conectarControlador(CtlPagoLista c){
		this.addWindowListener(c);
		rdbtnTodos.addActionListener(c);
		rdbtnTodos.setActionCommand("TODAS");
		

		btnReporte.addActionListener(c);
		btnReporte.setActionCommand("REPORTE");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ESCRIBIR");
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("ESCRIBIR");

		rdbtnRef.addActionListener(c);
		rdbtnRef.setActionCommand("ESCRIBIR");

		btnSiguiente.addActionListener(c);
		btnSiguiente.setActionCommand("NEXT");

		btnAnterior.addActionListener(c);
		btnAnterior.setActionCommand("LAST");

		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 
		 btnCobrador.addActionListener(c);
		 btnCobrador.setActionCommand("IMPRIMIR");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}


}
