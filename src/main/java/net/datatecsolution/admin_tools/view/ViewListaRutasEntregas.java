package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlRutasEntregas;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmRutasEntregas;

import javax.swing.*;
import java.awt.*;

public class ViewListaRutasEntregas extends ViewTabla {
	
	public TmRutasEntregas modelo;
	private BotonImprimirSmall btnImprimir;
		
	
	

	public ViewListaRutasEntregas(Window view) {
		//super("Proveedores");
				super(view,"Rutas de entregas");
				
				 btnImprimir = new BotonImprimirSmall();
				    //btnImprimir.setEnabled(false);
				    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
				    panelAccion.add(btnImprimir);
				
				modelo = new TmRutasEntregas();//se crea el modelo de los datos de la tabla
				tabla.setModel(modelo);
				//Estitlo para la tabla		
				TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
				tabla.setDefaultRenderer(String.class, renderizador);
				//tama�o de las columnas
				tabla.getColumnModel().getColumn(0).setPreferredWidth(50);     //Tama�o de las columnas de las tablas
				tabla.getColumnModel().getColumn(1).setPreferredWidth(400);	//de las columnas
				tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//en la tabla
				tabla.getColumnModel().getColumn(3).setPreferredWidth(100);	//
				//tabla.getColumnModel().getColumn(4).setPreferredWidth(50);	//
				//tabla.getColumnModel().getColumn(5).setPreferredWidth(50);	//
				
						
				rdbtnFecha.setVisible(true);
				
				//btnEliminar.setEnabled(true);
	
				
		        
		       // btnImprimir = new BotonImprimirSmall();
		        //btnLimpiar.setIcon(new ImageIcon(ViewListaProveedor.class.getResource("/View/imagen/clear.png"))); // NOI18N
		        //panelAccion.add(btnImprimir);
		
	}

	public TmRutasEntregas getModelo() {
		// TODO Auto-generated method stub
		return modelo;
	}
	
	public JTable getTabla(){
		return tabla;
	}
	public void conectarCtl(CtlRutasEntregas c){
		rdbtnTodos.addActionListener(c);
		rdbtnTodos.setActionCommand("TODAS");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("FECHA");
		
		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 
		
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
	}
	

}
