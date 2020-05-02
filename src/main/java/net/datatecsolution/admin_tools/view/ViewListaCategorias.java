package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCategoriaBuscar;
import net.datatecsolution.admin_tools.controlador.CtlCategoriaLista;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmCategorias;

import javax.swing.*;

public class ViewListaCategorias extends ViewTabla {
	
	
	
	protected JButton btnLimpiar;
	
	
	private JRadioButton rdbtnObservacion;
	private JRadioButton rdbtnCategoria;
	//protected JTextField txtBuscar;
	
	
	
	
	private TmCategorias modelo;
	/**
	 * @wbp.parser.constructor
	 */
	public ViewListaCategorias(JDialog view){
		super(view,"Buscar Categorias");
		Init();
		btnAgregar.setEnabled(false);
		btnLimpiar.setEnabled(false);
		
	}
	public ViewListaCategorias(JFrame view){
		super(view,"Categorias");
		Init();
		
	}
	public void Init() {
        
        btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
      
		
		rdbtnCategoria = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnCategoria);
		grupoOpciones.add(rdbtnCategoria);
		
		rdbtnObservacion = new JRadioButton("Observacion",false);
		panelOpcioneBusqueda.add(rdbtnObservacion);
		grupoOpciones.add(rdbtnObservacion);
		

		//tabla y sus componentes
		modelo=new TmCategorias();
	
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);
		
		scrollPane.setViewportView(tabla);
	
	
	}

public JButton getBtnLimpiar(){
	return btnLimpiar;
}

public TmCategorias getModelo(){
	return modelo;
}


public JRadioButton getRdbtnObservacion(){
	return rdbtnObservacion;
}
public JRadioButton getRdbtnCategoria(){
	return rdbtnCategoria;
}

public void conectarControlador(CtlCategoriaLista c){
	
	
	
		rdbtnTodos.addItemListener(c);
		
		
		rdbtnId.addActionListener(c);
		rdbtnId.addItemListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnObservacion.addActionListener(c);
		rdbtnObservacion.addItemListener(c);
		rdbtnObservacion.setActionCommand("ESCRIBIR");
		
		rdbtnCategoria.addActionListener(c);
		rdbtnCategoria.addItemListener(c);
		rdbtnCategoria.setActionCommand("ESCRIBIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
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
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 this.addWindowListener(c);
	}

public void conectarControladorBusqueda(CtlCategoriaBuscar c){
	rdbtnTodos.addItemListener(c);
	
	
	rdbtnId.addActionListener(c);
	rdbtnId.addItemListener(c);
	//rdbtnId.getActionCommand();
	rdbtnId.setActionCommand("ID");
	
	rdbtnObservacion.addActionListener(c);
	rdbtnObservacion.addItemListener(c);
	rdbtnObservacion.setActionCommand("ESCRIBIR");
	
	rdbtnCategoria.addActionListener(c);
	rdbtnCategoria.addItemListener(c);
	rdbtnCategoria.setActionCommand("ESCRIBIR");
	
	btnBuscar.addActionListener(c);
	btnBuscar.setActionCommand("BUSCAR");
	
	 btnAgregar.addActionListener(c);
	 btnAgregar.setActionCommand("INSERTAR");
	 
	 btnEliminar.addActionListener(c);
	 btnEliminar.setActionCommand("ELIMINAR");
	 
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

}
