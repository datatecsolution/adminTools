package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlProveedorLista;
import net.datatecsolution.admin_tools.controlador.CtlProveedoresBuscar;
import net.datatecsolution.admin_tools.view.botones.BotonCuenta;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TablaModeloProveedor;

import javax.swing.*;
import java.awt.*;

public class ViewListaProveedor extends ViewTabla {
	
	
	public TablaModeloProveedor modelo;
	//protected JTextField txtBuscar;
	
	
	protected JButton btnLimpiar;
	
	private JRadioButton rdbtnNombre;
	private JRadioButton rdbtnDireccion;
	
	private BotonReporte btnReporte;
	private BotonCuenta btnCuenta;
	
	
	
	
	public ViewListaProveedor(Window view){
		//super("Proveedores");
		super(view,"Proveedores");
		
		btnLimpiar = new BotonLimpiar();
        panelAccion.add(btnLimpiar);
        
		btnReporte=new BotonReporte();
        panelAccion.add(btnReporte);
        
        btnCuenta=new BotonCuenta();
        panelAccion.add(btnCuenta);
		
		modelo = new TablaModeloProveedor();//se crea el modelo de los datos de la tabla
		tabla.setModel(modelo);

		//Estitlo para la tabla		
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		//tama�o de las columnas
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
		tabla.getColumnModel().getColumn(2).setPreferredWidth(50);
		tabla.getColumnModel().getColumn(3).setPreferredWidth(50);
		tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
		
				
		
		
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		rdbtnDireccion = new JRadioButton("Direccion",false);
		panelOpcioneBusqueda.add(rdbtnDireccion);
		grupoOpciones.add(rdbtnDireccion);
		
			
        
        


		
	}
	
	public TablaModeloProveedor getModelo(){
		return modelo;
		
	}
	
	public JButton getBtnEliminar(){
		return btnEliminar;
	}
	
	public JRadioButton getRdbtnNombre(){
		return rdbtnNombre;
	}
	public JRadioButton getRdbtnDireccion(){
		return rdbtnDireccion;
	}
	
	
	
	public void conectarControlador(CtlProveedorLista c){
		
		rdbtnTodos.addItemListener(c);
		
		rdbtnId.addActionListener(c);
		rdbtnId.addItemListener(c);
		rdbtnId.setActionCommand("ID");
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.addItemListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
		rdbtnDireccion.addActionListener(c);
		rdbtnDireccion.addItemListener(c);
		rdbtnDireccion.setActionCommand("ESCRIBIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 
		 
		 btnReporte.addActionListener(c);
		 btnReporte.setActionCommand("CUENTASCLIENTES");
		 
		 btnCuenta.addActionListener(c);
		 btnCuenta.setActionCommand("CUENTACLIENTE");
		 
		 btnLimpiar.addActionListener(c);
		 btnLimpiar.setActionCommand("LIMPIAR");
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	public void conectarControladorBuscar(CtlProveedoresBuscar c){
		
		//rdbtnTodos.addItemListener(c);
		
		rdbtnId.addActionListener(c);
		//rdbtnId.addItemListener(c);
		rdbtnId.setActionCommand("ESCRIBIR");
		rdbtnId.addKeyListener(c);
		
		rdbtnNombre.addActionListener(c);
		//rdbtnNombre.addItemListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		rdbtnNombre.addKeyListener(c);
		
		rdbtnDireccion.addActionListener(c);
		//rdbtnDireccion.addItemListener(c);
		rdbtnDireccion.setActionCommand("ESCRIBIR");
		rdbtnDireccion.addKeyListener(c);
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		btnBuscar.addKeyListener(c);
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		txtBuscar.addKeyListener(c);
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 btnAgregar.addKeyListener(c);
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 btnEliminar.addKeyListener(c);
		 
		 btnLimpiar.addActionListener(c);
		 btnLimpiar.setActionCommand("LIMPIAR");
		 btnLimpiar.addKeyListener(c);
		 
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

}
