package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlRutaEntrega;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.view.botones.*;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturaCompra;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCajas;
import net.datatecsolution.admin_tools.view.tablemodel.TmFacturasEntregas;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewCrearRuta extends JDialog {
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCancelar;
	private JTable tFacturas;
	private TmFacturasEntregas modeloFacturasEntregas;
	private JTextField txtNofact;
	private JTextField txtEmpleado;
	private BotonAgregar botonAgregar;
	private BotonEliminar botonEliminar;
	private JComboBox<Caja> cbxCajas;
	private CbxTmCajas modeloListaCajas;
	private JLabel lblFact;
	private JDateChooser dateFecha;
	private BotonActualizar btnActualizar;

	public ViewCrearRuta(Window view) {
		// TODO Auto-generated constructor stub
		this.setTitle("Ruta de entrega");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		
		modeloFacturasEntregas=new TmFacturasEntregas();
		getContentPane().setLayout(null);
		
		getContentPane().setBackground(PanelPadre.color1);
		
		JLabel lblCantida = new JLabel("Vendedor (F1)");
		lblCantida.setBounds(16, 6, 109, 16);
		getContentPane().add(lblCantida);
		
		JLabel lblConcepto = new JLabel("Fecha");
		lblConcepto.setBounds(392, 6, 61, 16);
		getContentPane().add(lblConcepto);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setBounds(16, 501, 136, 77);
		//btnGuardar.setBounds(16, 219, 117, 29);
		getContentPane().add(btnGuardar);
		
		//botones
		btnActualizar=new BotonActualizar();
		//btnActualizar.setSize(128, 66);
		btnActualizar.setBounds(16, 501, 136, 77);
		getContentPane().add(btnActualizar);
		btnActualizar.setVisible(false);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setBounds(188, 501, 136, 77);
		getContentPane().add(btnCancelar);
		
		txtEmpleado = new JTextField();
		txtEmpleado.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		txtEmpleado.setBounds(16, 20, 363, 40);
		txtEmpleado.setEditable(false);
		txtEmpleado.setColumns(10);
		getContentPane().add(txtEmpleado);
		
		JPanel panel = new JPanel();
		panel.setBackground(PanelPadre.color1);
		panel.setBounds(6, 66, 776, 428);
		getContentPane().add(panel);
		
		panel.setLayout(null);
		panel.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Facturas de la entrega", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 66, 742, 344);
		panel.add(scrollPane);
		
		tFacturas = new JTable();
		tFacturas.setRowHeight(25);
		scrollPane.setViewportView(tFacturas);
		tFacturas.setModel(modeloFacturasEntregas);
		
		RenderizadorTablaFacturaCompra renderizador = new RenderizadorTablaFacturaCompra();
		tFacturas.setDefaultRenderer(String.class, renderizador);
		
		
		tFacturas.getColumnModel().getColumn(0).setPreferredWidth(30);     //Tamaño de las columnas de las tablas
		tFacturas.getColumnModel().getColumn(1).setPreferredWidth(50);	//
		tFacturas.getColumnModel().getColumn(2).setPreferredWidth(50);	//
		tFacturas.getColumnModel().getColumn(3).setPreferredWidth(300);	//
		tFacturas.getColumnModel().getColumn(4).setPreferredWidth(50);	//
		
		botonEliminar = new BotonEliminar();
		botonEliminar.setBounds(718, 26, 40, 36);
		panel.add(botonEliminar);
		
		botonAgregar = new BotonAgregar();
		botonAgregar.setBounds(678, 26, 40, 36);
		panel.add(botonAgregar);
		
		txtNofact = new JTextField();
		txtNofact.setBounds(386, 26, 280, 40);
		panel.add(txtNofact);
		txtNofact.setColumns(10);
		
		JLabel lblAgregar = new JLabel("Facturado en");
		lblAgregar.setBounds(16, 35, 90, 22);
		panel.add(lblAgregar);
		
		modeloListaCajas=new CbxTmCajas();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaCajas.agregar(new Caja());
		
		cbxCajas = new JComboBox<Caja>(modeloListaCajas);
		cbxCajas.setBounds(107, 24, 214, 40);
		panel.add(cbxCajas);
		
		lblFact = new JLabel("# Fact");
		lblFact.setBounds(333, 38, 48, 16);
		panel.add(lblFact);
		
		dateFecha = new JDateChooser();
		dateFecha.setBackground(PanelPadre.color1);
		dateFecha.setDateFormatString("dd-MM-yyyy");
		dateFecha.setBounds(392, 20, 328, 40);
		getContentPane().add(dateFecha);
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		//flowLayout_1.setHgap(1);
		
		
		this.setSize(800, 610);
		
		setResizable(false);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	
	
	public void conectarControlador(CtlRutaEntrega c){
		/*
		tFacturas.addKeyListener(c);
		tFacturas.addMouseListener(c);
		modeloFacturasEntregas.addTableModelListener(c);
		tFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//tFacturas.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tFacturas.setColumnSelectionAllowed(true);
		tFacturas.setRowSelectionAllowed(false);
		tFacturas.setCellSelectionEnabled(false);
		*/
		
		tFacturas.addMouseListener(c);
		modeloFacturasEntregas.addTableModelListener(c);
		 tFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		btnGuardar.setActionCommand("GUARDAR");
		btnGuardar.addActionListener(c);
		
		btnActualizar.setActionCommand("ACTUALIZAR");
		btnActualizar.addActionListener(c);
		
		btnCancelar.setActionCommand("CANCELAR");
		btnCancelar.addActionListener(c);
		
		txtNofact.setActionCommand("BUSCAR");
		txtNofact.addActionListener(c);
		
		botonAgregar.setActionCommand("BUSCAR");
		botonAgregar.addActionListener(c);
		
		botonEliminar.setActionCommand("ELIMINAR");
		botonEliminar.addActionListener(c);
		
		
		btnGuardar.addKeyListener(c);
		btnCancelar.addKeyListener(c);
		txtEmpleado.addKeyListener(c);
		dateFecha.addKeyListener(c);
		tFacturas.addKeyListener(c);
		botonAgregar.addKeyListener(c);
		botonEliminar.addKeyListener(c);
		txtNofact.addKeyListener(c);
	
		
		
	}
	


	/**
	 * @return the tFacturas
	 */
	public JTable gettFacturas() {
		return tFacturas;
	}



	/**
	 * @param tFacturas the tFacturas to set
	 */
	public void settFacturas(JTable tFacturas) {
		this.tFacturas = tFacturas;
	}



	/**
	 * @return the modeloFacturasEntregas
	 */
	public TmFacturasEntregas getModeloFacturasEntregas() {
		return modeloFacturasEntregas;
	}



	/**
	 * @param modeloFacturasEntregas the modeloFacturasEntregas to set
	 */
	public void setModeloFacturasEntregas(TmFacturasEntregas modeloFacturasEntregas) {
		this.modeloFacturasEntregas = modeloFacturasEntregas;
	}



	/**
	 * @return the txtEmpleado
	 */
	public JTextField getTxtEmpleado() {
		return txtEmpleado;
	}



	/**
	 * @return the cbxCajas
	 */
	public JComboBox<Caja> getCbxCajas() {
		return cbxCajas;
	}



	/**
	 * @return the modeloListaCajas
	 */
	public CbxTmCajas getModeloListaCajas() {
		return modeloListaCajas;
	}



	/**
	 * @return the txtNofact
	 */
	public JTextField getTxtNofact() {
		return txtNofact;
	}



	/**
	 * @return the dateFecha
	 */
	public JDateChooser getDateFecha() {
		return dateFecha;
	}
	
	public JTable getTablasFacturas(){
		return this.tFacturas;
	}
	public TmFacturasEntregas getModeloFacturas(){
		return this.modeloFacturasEntregas;
	}



	/**
	 * @return the botonEliminar
	 */
	public BotonEliminar getBotonEliminar() {
		return botonEliminar;
	}



	/**
	 * @return the btnGuardar
	 */
	public BotonGuardar getBtnGuardar() {
		return btnGuardar;
	}



	/**
	 * @return the btnActualizar
	 */
	public BotonActualizar getBtnActualizar() {
		return btnActualizar;
	}
}
