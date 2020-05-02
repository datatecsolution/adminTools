package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCotizacionLista;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturas;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturas;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ViewListaCotizacion extends ViewTabla {
	
	protected BotonImprimirSmall btnImprimir;
	
	
	
	private JRadioButton rdbtnCliente;

	
	
	private static final DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
	
	
	
	private TmFacturas modelo;
	
	public ViewListaCotizacion(Window view){
		
		super(view,"Cotizaciones");
		
		
		
		
        btnImprimir = new BotonImprimirSmall();
       // btnImprimir.setEnabled(false);
        //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
        panelAccion.add(btnImprimir);
        
    
        
        
        rdbtnFecha.setVisible(true);
	
        rdbtnCliente=new JRadioButton("Cliente");
		panelOpcioneBusqueda.add(rdbtnCliente);
		grupoOpciones.add(rdbtnCliente);
		
	
		
		
				
	
        //tabla y sus componentes
		modelo=new TmFacturas();
		
		
		tabla.setModel(modelo);
		RenderizadorTablaFacturas renderizador = new RenderizadorTablaFacturas();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(100);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(300);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(70);	//en la tabla
		tabla.getColumnModel().getColumn(2).setPreferredWidth(70);	//
		
		
	}
	
	
public void conectarControlador(CtlCotizacionLista c){
		
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnCliente.addActionListener(c);
		rdbtnCliente.setActionCommand("ESCRIBIR");
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("MARCA");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
	
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	public TmFacturas getModelo(){
		return modelo;
	}
	
	
	public BotonImprimirSmall getBtnCobrar(){
		return btnImprimir;
	}
	public JRadioButton getRdbtnCliente(){
		return rdbtnCliente;
	}
	

	

}
