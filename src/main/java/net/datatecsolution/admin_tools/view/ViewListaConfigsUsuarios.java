package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlConfigUsuariosLista;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TrConfigUsers2;
import net.datatecsolution.admin_tools.view.tablemodel.TmConfigUser;

import javax.swing.*;

public class ViewListaConfigsUsuarios extends ViewTabla {
	
	
	protected JButton btnLimpiar;
	
	
	private JRadioButton rdbtnUser;
	private JRadioButton rdbtnNombre;
	
	
	
	private TmConfigUser modelo;


	
	/**
	 * @wbp.parser.constructor
	 */
	public ViewListaConfigsUsuarios(JDialog view){
		
		super(view,"Configuraciones de usuarios");
		Init();
		btnAgregar.setEnabled(false);
		btnLimpiar.setEnabled(false);
		
	}
	public ViewListaConfigsUsuarios(JFrame view){
		super(view,"Configuraciones de usuarios");
		Init();
		
	}
	
	
	public void Init() {
		
		
        
        btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
        
       
		
		//opciones de busquedas
		rdbtnUser = new JRadioButton("User",false);
		panelOpcioneBusqueda.add(rdbtnUser);
		grupoOpciones.add(rdbtnUser);
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		
	
		
		//tabla y sus componentes
		modelo=new TmConfigUser();
		
		tabla.setModel(modelo);
		TrConfigUsers2 renderizador2 = new TrConfigUsers2();
		tabla.setDefaultRenderer(String.class, renderizador2);
		
		
		//tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		//tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		//tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);
		
		//setSize(718,591);
		
		this.btnEliminar.setEnabled(false);
	
	
	}
	
	public TmConfigUser getModelo(){
		return modelo;
	}
	
	public JRadioButton getRdbtnNombre(){
		return rdbtnNombre;
	}
	public JRadioButton getRdbtnUser(){
		return rdbtnUser;
	}
	public JRadioButton getRdbtnTodos(){
		return rdbtnTodos;
	}
	
	
	public void conectarCtl(CtlConfigUsuariosLista c){
		
		
		
		
		
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("INSERTAR");
		
		btnEliminar.addActionListener(c);
		btnEliminar.setActionCommand("ELIMINAR");
		
		rdbtnUser.addActionListener(c);
		rdbtnUser.setActionCommand("ESCRIBIR");
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCAR");
		
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		
		
		
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	}
	
}
