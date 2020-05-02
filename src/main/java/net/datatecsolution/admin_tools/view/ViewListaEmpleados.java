package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlEmpleadosLista;
import net.datatecsolution.admin_tools.controlador.CtlEmpleadosListaBuscar;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmEmpleados;

import javax.swing.*;
import java.awt.*;

public class ViewListaEmpleados extends ViewTabla  {
	
	
	protected JButton btnLimpiar;
	
	
	
	private JRadioButton rdbtnApellido;
	private JRadioButton rdbtnNombre;

	
	private TmEmpleados modelo;
	
	public ViewListaEmpleados(Window view){
		super(view,"Empleados");
		Init();
	}
	
	public void Init() {
		
		
        
        btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		rdbtnApellido = new JRadioButton("Apellido",false);
		panelOpcioneBusqueda.add(rdbtnApellido);
		grupoOpciones.add(rdbtnApellido);
		
		
		
		//tabla y sus componentes
		modelo=new TmEmpleados();
		
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);

	}
	
	public TmEmpleados getModelo(){
		return modelo;
	}
	
	public void conectarCtl(CtlEmpleadosLista c){
		
		
		rdbtnApellido.addActionListener(c);
		rdbtnApellido.setActionCommand("ESCRIBIR");
		
		
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("INSERTAR");
		
		btnEliminar.addActionListener(c);
		btnEliminar.setActionCommand("ELIMINAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		
		
	}
	
	public JRadioButton getRdbtnApellido(){
		return rdbtnApellido;
	}
	public JRadioButton getRdbtnNombre(){
		return rdbtnNombre;
	}
	
	public void conectarCtlBuscar(CtlEmpleadosListaBuscar c) {
		// TODO Auto-generated method stub
		this.addWindowListener(c);
		
		rdbtnApellido.addActionListener(c);
		rdbtnApellido.setActionCommand("ESCRIBIR");
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("INSERTAR");
		btnAgregar.addKeyListener(c);
		
		btnEliminar.addActionListener(c);
		btnEliminar.setActionCommand("ELIMINAR");
		btnAgregar.addKeyListener(c);
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		btnAgregar.addKeyListener(c);
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		btnAgregar.addKeyListener(c);
		
		btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 btnAgregar.addKeyListener(c);
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 btnAgregar.addKeyListener(c);
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 txtBuscar.addKeyListener(c);
		
		
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	}

}
