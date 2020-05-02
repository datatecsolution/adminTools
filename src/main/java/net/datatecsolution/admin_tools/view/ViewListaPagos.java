package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlPagoLista;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.TmPagos;

import javax.swing.*;
import java.awt.*;

public class ViewListaPagos extends ViewTabla {
	
	
	protected BotonImprimirSmall btnImprimir;
	
	
	
	
	
	
	
	private TmPagos modelo;







	private BotonReporte btnReporte;




	

	public ViewListaPagos(Window view) {
		super(view,"Pagos de clientes");
		
		
		
	
	    
	    btnImprimir = new BotonImprimirSmall();
	    btnImprimir.setEnabled(false);
	    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
	    panelAccion.add(btnImprimir);
	    //panelAccion.setVisible(false);
	    
	    btnReporte=new BotonReporte();
	    panelAccion.add(btnReporte);
	    
	 
		
	    rdbtnFecha.setVisible(true);
		
		
				
		
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
	
	public BotonImprimirSmall getBtnImprimir(){
		return btnImprimir;
	}

	
public void conectarControlador(CtlPagoLista c){
		this.addWindowListener(c);
		rdbtnTodos.addActionListener(c);
		rdbtnTodos.setActionCommand("TODAS");
		

		btnReporte.addActionListener(c);
		btnReporte.setActionCommand("REPORTE");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("FECHA");
		
		
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		// super.bbtnLimpiar.addActionListener(c);
		// btnLimpiar.setActionCommand("LIMPIAR");
		
		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}


}
