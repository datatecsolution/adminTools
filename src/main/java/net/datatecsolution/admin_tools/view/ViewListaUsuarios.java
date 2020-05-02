package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlUsuariosLista;
import net.datatecsolution.admin_tools.view.botones.BotonCaja;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmUsuarios;

import javax.swing.*;

public class ViewListaUsuarios extends ViewTabla {
	
	
	protected JButton btnLimpiar;
	
	
	private JRadioButton rdbtnUser;
	private JRadioButton rdbtnNombre;
	
	
	
	private TmUsuarios modelo;


	private BotonCaja btnCajas;
	/**
	 * @wbp.parser.constructor
	 */
	public ViewListaUsuarios(JDialog view){
		
		super(view,"Buscar Usuarios");
		Init();
		btnAgregar.setEnabled(false);
		btnLimpiar.setEnabled(false);
		
	}
	public ViewListaUsuarios(JFrame view){
		super(view,"Usuarios");
		Init();
		
	}
	
	
	public void Init() {
		
		
        
        btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
        btnCajas=new BotonCaja();
        panelAccion.add(btnCajas);
        btnCajas.setVisible(false);
       
		
		//opciones de busquedas
		rdbtnUser = new JRadioButton("User",false);
		panelOpcioneBusqueda.add(rdbtnUser);
		grupoOpciones.add(rdbtnUser);
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		
	
		
		//tabla y sus componentes
		modelo=new TmUsuarios();
		
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);
		
		//setSize(718,591);
		
		this.btnEliminar.setEnabled(false);
	
	
	}
	
	public TmUsuarios getModelo(){
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
	
	
	public void conectarCtl(CtlUsuariosLista c){
		
		
		btnCajas.addActionListener(c);
		btnCajas.setActionCommand("CAJAS");
		
		
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
