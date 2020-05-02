package net.datatecsolution.admin_tools.view;


import net.datatecsolution.admin_tools.controlador.CtlArticulo;
import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.Impuesto;
import net.datatecsolution.admin_tools.view.botones.*;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.rendes.RtPrecios;
import net.datatecsolution.admin_tools.view.tablemodel.ComboBoxImpuesto;
import net.datatecsolution.admin_tools.view.tablemodel.ListaModeloCodBarra;
import net.datatecsolution.admin_tools.view.tablemodel.TmInsumo;
import net.datatecsolution.admin_tools.view.tablemodel.TmPrecios;

import javax.swing.*;
import java.awt.*;


public class ViewCrearArticulo extends JDialog {
	private JLabel lblNombre;
	private JTextField txtNombre;
	private JLabel lblMarca;
	private JComboBox<Impuesto> cbxImpuesto ;
	private JLabel lblImpuesto;
	private Font myFont;
	private BotonCancelar btnCancelar;
	private BotonGuardar btnGuardar;
	private BotonActualizar btnActualizar;
	
	private JTextField txtMarca;
	private JButton btnBuscar;
	private JList<CodBarra> listCodigos;
	private JTextField txtCodigo;
	private JComboBox<String> cbxTipo;
	private TmPrecios modeloPrecio;
	private TmInsumo modeloInsumos;
	
	
	private JMenuItem mntmEliminar;
	private JMenuItem mntmImprimir;
	

	
	private JPopupMenu menuContextual; // permite al usuario seleccionar el color
	
	
	
	//se crea el modelo de la lista de los impuestos
	private ComboBoxImpuesto modeloImpuesto;
	
	private ListaModeloCodBarra modeloCodBarra;
	private JTable tblPrecios;
	private JScrollPane scrollPane_1;
	private Color colorBorde;
	private JLabel label;
	private JPanel panel_1;
	private JTable tInsumos;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JButton btnAgregarInsumo;
	private JPanel pInsumo;
	private BotonEliminar btnEliminarInsumo;
	private JTabbedPane tpInsumos;
	private JPanel panel;
	private JPanel panel_6;
	private JTextField txtInsumoTotal;
	
	

	public ViewCrearArticulo(ViewListaArticulo view) {
		
		super(view,"Agregar Articulos", ModalityType.DOCUMENT_MODAL);
		
		setFont(new Font("Verdana", Font.PLAIN, 12));
		setResizable(false);
		//getContentPane().setBackground(PanelPadre.color1);
		myFont=new Font("Verdana", Font.PLAIN, 12);
		
		modeloInsumos=new TmInsumo();
		
		colorBorde=Color.DARK_GRAY;
		getContentPane().setBackground(PanelPadre.color1);
		
		
		menuContextual = new JPopupMenu(); // crea el men� contextual
		
		//opcion del menu flotante
		mntmEliminar = new JMenuItem("Eliminar");
		menuContextual.add(mntmEliminar);
		
		
		
		//opcion del menu flotante
		mntmImprimir = new JMenuItem("Imprimir");
		menuContextual.add(mntmImprimir);
		
		modeloCodBarra=new ListaModeloCodBarra();
		this.modeloPrecio=new TmPrecios();
		
		RtPrecios renderizador = new RtPrecios();
		getContentPane().setLayout(null);
		
		
		modeloImpuesto=new ComboBoxImpuesto();
		Impuesto uno=new Impuesto();
		modeloImpuesto.agregar(uno);
		
		label = new JLabel("");
		label.setBounds(258, 389, 0, 0);
		getContentPane().add(label);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setBounds(333, 588, 136, 64);
		getContentPane().add(btnCancelar);
		
		
		
		//botones
		btnGuardar = new BotonGuardar();
		btnGuardar.setBounds(102, 588, 136, 64);
		getContentPane().add(btnGuardar);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setBounds(102, 588, 136, 64);
		getContentPane().add(btnActualizar);
		
		tpInsumos = new JTabbedPane(JTabbedPane.TOP);
		//tabbedPane_1.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		tpInsumos.setBounds(10, 16, 578, 560);
		getContentPane().add(tpInsumos);
		
		panel = new JPanel();
		tpInsumos.addTab("General", null, panel, null);
		panel.setBackground(PanelPadre.color2);
		panel.setLayout(null);
		
		
		//Nombre Articulo
		lblNombre=new JLabel();
		lblNombre.setBounds(6, 6, 97, 15);
		panel.add(lblNombre);
		lblNombre.setText("Descripcion");
		lblNombre.setFont(myFont);
		
		txtNombre=new JTextField(30);
		txtNombre.setBounds(6, 26, 533, 25);
		panel.add(txtNombre);
		txtNombre.setFont(myFont);
		
		JLabel lblTipo = new JLabel("Tipo");
		lblTipo.setBounds(6, 63, 53, 15);
		panel.add(lblTipo);
		lblTipo.setFont(myFont);
		
		cbxTipo = new JComboBox<String>();
		cbxTipo.setBounds(6, 79, 533, 27);
		panel.add(cbxTipo);
		cbxTipo.setModel(new DefaultComboBoxModel(new String[] {"Bienes", "Servicio"}));
		
		
		//Marca
		lblMarca = new JLabel("Categoria");
		lblMarca.setBounds(6, 118, 79, 15);
		panel.add(lblMarca);
		lblMarca.setFont(myFont);
		
		panel_1 = new JPanel();
		panel_1.setBounds(6, 145, 526, 29);
		panel.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		txtMarca = new JTextField();
		panel_1.add(txtMarca, BorderLayout.CENTER);
		txtMarca.setEditable(false);
		txtMarca.setColumns(10);
		
		btnBuscar = new JButton("...");
		panel_1.add(btnBuscar, BorderLayout.EAST);
		
		
		//Impuesto
		lblImpuesto = new JLabel("Impuesto");
		lblImpuesto.setBounds(6, 186, 79, 15);
		panel.add(lblImpuesto);
		lblImpuesto.setFont(myFont);
		
		
		cbxImpuesto = new JComboBox<Impuesto>();
		cbxImpuesto.setBounds(6, 202, 533, 27);
		panel.add(cbxImpuesto);
		cbxImpuesto.setModel(modeloImpuesto);
		cbxImpuesto.setFont(myFont);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(10, 237, 529, 119);
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);
		listCodigos = new JList<CodBarra>();
		scrollPane.setViewportView(listCodigos);
		listCodigos.setModel(modeloCodBarra);
		listCodigos.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		panel_4 = new JPanel();
		scrollPane.setColumnHeaderView(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));
		
				
		JLabel lblCodigoBarra = new JLabel("Codigo Barra");
		panel_4.add(lblCodigoBarra,BorderLayout.NORTH);
		lblCodigoBarra.setFont(myFont);
		
		txtCodigo = new JTextField();
		panel_4.add(txtCodigo, BorderLayout.CENTER);
		txtCodigo.setColumns(10);
		
		panel_3 = new JPanel();
		panel_3.setBounds(10, 368, 529, 109);
		panel.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblOtrosPrecios = new JLabel("Precios Venta");
		panel_3.add(lblOtrosPrecios, BorderLayout.NORTH);
		lblOtrosPrecios.setFont(myFont);
		//tblPrecios.setBounds(118, 362, 1, 1);
		//getContentPane().add(table);
		
		
		
		tblPrecios = new JTable();
		tblPrecios.setBounds(2, 18, 643, 0);
		getContentPane().add(tblPrecios);
		tblPrecios.setModel(modeloPrecio);
		tblPrecios.setDefaultRenderer(String.class, renderizador);
		
		scrollPane_1 = new JScrollPane(tblPrecios);
		panel_3.add(scrollPane_1, BorderLayout.CENTER);
		
		panel_6 = new JPanel();
		tpInsumos.addTab("Insumos", null, panel_6, null);
		tpInsumos.setEnabledAt(1, false);
		panel_6.setBackground(PanelPadre.color2);
		panel_6.setLayout(null);
		
		pInsumo = new JPanel();
		pInsumo.setBounds(18, 43, 533, 420);
		panel_6.add(pInsumo);
		pInsumo.setLayout(new BorderLayout(0, 0));
		
		panel_5 = new JPanel();
		pInsumo.add(panel_5, BorderLayout.NORTH);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_2 = new JScrollPane();
		pInsumo.add(scrollPane_2, BorderLayout.CENTER);
		
		tInsumos = new JTable();
		scrollPane_2.setViewportView(tInsumos);
		tInsumos.setModel(modeloInsumos);
		
		btnAgregarInsumo = new BotonAgregar();
		btnAgregarInsumo.setBounds(18, 6, 40, 36);
		panel_6.add(btnAgregarInsumo);
		
		btnEliminarInsumo = new BotonEliminar();
		btnEliminarInsumo.setBounds(64, 6, 40, 36);
		panel_6.add(btnEliminarInsumo);
		
		JLabel lblTotal = new JLabel("Total");
		lblTotal.setBounds(292, 480, 61, 16);
		panel_6.add(lblTotal);
		
		txtInsumoTotal = new JTextField();
		txtInsumoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtInsumoTotal.setBounds(343, 475, 208, 26);
		panel_6.add(txtInsumoTotal);
		txtInsumoTotal.setColumns(10);
		
		tInsumos.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tamaño de las columnas de las tablas
		tInsumos.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tInsumos.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		btnActualizar.setVisible(false);
		
		setSize(592,687);
		
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
	
		
	}
	
	public void conectarCtl(CtlArticulo m){
		
		
		tInsumos.addKeyListener(m);
		tInsumos.addMouseListener(m);
		modeloInsumos.addTableModelListener(m);
		//tableDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tInsumos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tInsumos.setColumnSelectionAllowed(true);
		tInsumos.setRowSelectionAllowed(true);
		tInsumos.setCellSelectionEnabled(true);
		
		btnEliminarInsumo.addActionListener(m);
		btnEliminarInsumo.setActionCommand("ELIMINAR_INSUMO");
		
		btnAgregarInsumo.addActionListener(m);
		btnAgregarInsumo.setActionCommand("AGREGAR_INSUMO");
		
		btnGuardar.addActionListener(m);
		btnGuardar.setActionCommand("GUARDAR");
		
		
		btnBuscar.addActionListener(m);
		btnBuscar.setActionCommand("BUSCAR");
		
		btnActualizar.addActionListener(m);
		btnActualizar.setActionCommand("ACTUALIZAR");

		btnCancelar.addActionListener(m);
		btnCancelar.setActionCommand("CANCELAR");
		
		txtCodigo.addActionListener(m);
		txtCodigo.setActionCommand("NUEVOCODIGO");
		 
		listCodigos.addKeyListener(m);
		listCodigos.addMouseListener(m);
		
		mntmEliminar.addActionListener(m);
		mntmEliminar.setActionCommand("ELIMINARCODIGO");
		
		
		mntmImprimir.addActionListener(m);
		mntmImprimir.setActionCommand("IMPRIMIR");
		
		cbxTipo.addActionListener(m);
		cbxTipo.setActionCommand("CAMBIOCOMBOBOX");
	}
	public TmPrecios getModeloPrecio(){
		return modeloPrecio;
	}
	public JTable getTablaPrecios(){
		return this.tblPrecios;
	}
	public BotonActualizar getBtnActualizar(){
		return btnActualizar;
	}
	public JButton getBtnGuardar(){
		return btnGuardar;
	}
	public JTextField getTxtMarca(){
		return txtMarca;
	}
	public JTextField getTxtNombre(){
		return txtNombre;
	}
	public JComboBox getCbxImpuesto(){
		return cbxImpuesto;
	}
	
	public JComboBox getCbxTipo(){
		return cbxTipo;
	}
	
	public ComboBoxImpuesto getListaCbxImpuesto(){
		return modeloImpuesto;
	}
	
	public ListaModeloCodBarra getModeloCodBarra(){
		return modeloCodBarra;
	}
	public JTextField getTxtCodigo(){
		return txtCodigo;
	}
	
	public void configActualizar(){
		btnGuardar.setVisible(false);
		btnActualizar.setVisible(true);
		this.setTitle("Actualizar Articulo");
	}
	public JList getListCodigos(){
		return listCodigos;
	}
	
	public JPopupMenu getMenuContextual(){
		return menuContextual;
		
	}

	/**
	 * @return the pInsumo
	 */
	public JPanel getpInsumo() {
		return pInsumo;
	}

	/**
	 * @return the tInsumos
	 */
	public JTable gettInsumos() {
		return tInsumos;
	}

	/**
	 * @return the btnAgregarInsumo
	 */
	public JButton getBtnAgregarInsumo() {
		return btnAgregarInsumo;
	}

	/**
	 * @return the modeloInsumos
	 */
	public TmInsumo getModeloInsumos() {
		return modeloInsumos;
	}

	/**
	 * @return the btnEliminarInsumo
	 */
	public BotonEliminar getBtnEliminarInsumo() {
		return btnEliminarInsumo;
	}

	/**
	 * @return the tpInsumos
	 */
	public JTabbedPane getTpInsumos() {
		return tpInsumos;
	}

	/**
	 * @return the txtInsumoTotal
	 */
	public JTextField getTxtInsumoTotal() {
		return txtInsumoTotal;
	}
}
