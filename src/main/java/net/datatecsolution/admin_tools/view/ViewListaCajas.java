package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCajasBuscar;
import net.datatecsolution.admin_tools.controlador.CtlCajasLista;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmCajas;

import javax.swing.*;
import java.awt.*;


public class ViewListaCajas  extends ViewTabla  {
	
	private TmCajas modelo;
	private JRadioButton rdbtnDescripcion;
	private BotonLimpiar btnLimpiar;

	public ViewListaCajas(Window view) {
		super(view, "Cajas de facturacion");
		// TODO Auto-generated constructor stub
		 btnLimpiar = new BotonLimpiar();
	        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
	     panelAccion.add(btnLimpiar);
		
		rdbtnDescripcion = new JRadioButton("Descripcion",false);
		panelOpcioneBusqueda.add(rdbtnDescripcion);
		grupoOpciones.add(rdbtnDescripcion);
		
		//tabla y sus componentes
		modelo=new TmCajas();
	
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);
		
		scrollPane.setViewportView(tabla);
	}

	/**
	 * @return the modelo
	 */
	public TmCajas getModelo() {
		return modelo;
	}

	/**
	 * @return the rdbtnDescripcion
	 */
	public JRadioButton getRdbtnDescripcion() {
		return rdbtnDescripcion;
	}

	
	
	
	public void conectarCtlBuscar(CtlCajasBuscar c){
		
		
		
		
		rdbtnId.addActionListener(c);
		rdbtnId.addItemListener(c);
		rdbtnId.setActionCommand("ID");
		
		rdbtnDescripcion.addActionListener(c);
		rdbtnDescripcion.addItemListener(c);
		rdbtnDescripcion.setActionCommand("DESCRIPCION");
		
		rdbtnDescripcion.addActionListener(c);
		rdbtnDescripcion.addItemListener(c);
		rdbtnDescripcion.setActionCommand("ESCRIBIR");
		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		rdbtnDescripcion.addActionListener(c);
		rdbtnDescripcion.addItemListener(c);
		rdbtnDescripcion.setActionCommand("ESCRIBIR");
		
		
		
		
		 btnLimpiar.addActionListener(c);
		 btnLimpiar.setActionCommand("LIMPIAR");
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
	}

	
	public void conectarCtl(CtlCajasLista c) {
		// TODO Auto-generated method stub
		
		rdbtnTodos.addItemListener(c);
		
		
		rdbtnId.addActionListener(c);
		rdbtnId.addItemListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnDescripcion.addActionListener(c);
		rdbtnDescripcion.addItemListener(c);
		rdbtnDescripcion.setActionCommand("ESCRIBIR");
		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 btnLimpiar.addActionListener(c);
		 btnLimpiar.setActionCommand("LIMPIAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 this.addWindowListener(c);
		
		
		
	}

}
