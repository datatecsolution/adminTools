package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlRequisicion;
import net.datatecsolution.admin_tools.view.botones.BotonBuscar1;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmDepartamento;
import net.datatecsolution.admin_tools.view.tablemodel.TabloModeloRequisicion;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewRequisicion extends JDialog {
	
	private JPanel panelAcciones;
	private JPanel panelBuscar;
	private JPanel panelDatosFactura;
	
	private JTable tableDetalle;
	private TabloModeloRequisicion modeloTabla;
	private JLabel label;
	private JTextField txtFecha;
	private JLabel lblDepartamento;
	
	private JTextField txtBuscar;
	private JTextField txtTotal;
	private CbxTmDepartamento cbxModeloOrigen;
	private CbxTmDepartamento cbxModeloDestino;
	private JComboBox cbxDepatOrigen;
	private JComboBox cbxDepartDestino;
	
	private BotonGuardar btnGuardar;
	private BotonCancelar btnCerrar;
	private BotonBuscar1 btnBuscar;

	
	
	
	public ViewRequisicion(Window view){
		super(view,"Requisicion de articulos", ModalityType.DOCUMENT_MODAL);
		Font myFont=new Font("OCR A Extended", Font.PLAIN, 45);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);// evente del ocultar la ventana
			}
		});
		
		panelAcciones=new PanelPadre();
		panelAcciones.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Opciones", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAcciones.setBounds(20, 11, 164, 459);
		panelAcciones.setLayout(null);
		
		getContentPane().add(panelAcciones);
		
		getContentPane().setBackground(PanelPadre.color1);
		
		
		panelDatosFactura=new PanelPadre();
		//panelDatosFactura.setBackground(Color.WHITE);
		panelDatosFactura.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Datos Generales", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelDatosFactura.setBounds(208, 11, 709, 73);
		panelDatosFactura.setLayout(null);
		
		getContentPane().add(panelDatosFactura);
		
		label = new JLabel("Fecha");
		label.setBounds(10, 11, 40, 29);
		panelDatosFactura.add(label);
		
		txtFecha = new JTextField();
		txtFecha.setEditable(false);
		txtFecha.setColumns(10);
		txtFecha.setBounds(10, 32, 104, 29);
		panelDatosFactura.add(txtFecha);
		
		lblDepartamento = new JLabel("Departamento Origen de los Articulos");
		lblDepartamento.setBounds(124, 11, 249, 29);
		panelDatosFactura.add(lblDepartamento);
		
		cbxModeloOrigen= new CbxTmDepartamento();
		cbxModeloDestino= new CbxTmDepartamento();
		
		cbxDepatOrigen = new JComboBox();
		cbxDepatOrigen.setModel(cbxModeloOrigen);//comentar para ver la view
		cbxDepatOrigen.setBounds(124, 32, 235, 30);
		panelDatosFactura.add(cbxDepatOrigen);
		
		JLabel lblDepartamentoDestinoDe = new JLabel("Departamento Destino de los Articulos");
		lblDepartamentoDestinoDe.setBounds(399, 11, 282, 29);
		panelDatosFactura.add(lblDepartamentoDestinoDe);
		
		cbxDepartDestino = new JComboBox();
		cbxDepartDestino.setModel(cbxModeloDestino);//comentar para ver la view
		cbxDepartDestino.setBounds(399, 32, 282, 29);
		panelDatosFactura.add(cbxDepartDestino);
		
		
		
		panelBuscar= new PanelPadre();
		panelBuscar.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Buscar Articulo", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelBuscar.setBounds(208, 95, 709, 73);
		panelBuscar.setLayout(null);
		//getContentPane().geti
		getContentPane().add(panelBuscar);
		
		
		tableDetalle = new JTable();
		modeloTabla=new TabloModeloRequisicion();
		tableDetalle.setModel(modeloTabla);
		
		JScrollPane scrollPane = new JScrollPane(tableDetalle);
		scrollPane.setBounds(208, 182, 709, 284);
		getContentPane().add(scrollPane);
		
		
		
		txtBuscar = new JTextField();
		txtBuscar.setBounds(10, 28, 676, 32);
		panelBuscar.add(txtBuscar);
		txtBuscar.setColumns(10);
		
		JLabel lblTotal = new JLabel("Total");
		lblTotal.setBounds(697, 490, 46, 14);
		getContentPane().add(lblTotal);
		
		txtTotal = new JTextField();
		txtTotal.setForeground(Color.RED);
		txtTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		txtTotal.setFont(myFont);
		txtTotal.setText("00");
		txtTotal.setEditable(false);
		txtTotal.setBounds(697, 506, 220, 53);
		getContentPane().add(txtTotal);
		txtTotal.setColumns(10);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setText("F4 Guardar");
		btnGuardar.setBounds(10, 115, 144, 80);
		panelAcciones.add(btnGuardar);
		
		btnCerrar = new BotonCancelar();
		btnCerrar.setText("Esc Cerrar");
		btnCerrar.setBounds(10, 206, 144, 80);
		panelAcciones.add(btnCerrar);
		
		btnBuscar = new BotonBuscar1();
		btnBuscar.setBounds(10, 24,144, 80);
		panelAcciones.add(btnBuscar);
		
		this.setSize(973, 605);
		getContentPane().setLayout(null);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	public CbxTmDepartamento getCbxModeloOrigen(){
		return cbxModeloOrigen;
	}
	public CbxTmDepartamento getCbxModeloDestino(){
		return cbxModeloDestino;
	}
	
	public JComboBox getCbxDepartDestino(){
		return cbxDepartDestino;
	}
	public JComboBox getCbxDepatOrigen(){
		return cbxDepatOrigen;
	}


	public TabloModeloRequisicion getModelo() {
		// TODO Auto-generated method stub
		return modeloTabla;
	}


	public JTable getTablaArticulos() {
		// TODO Auto-generated method stub
		return tableDetalle;
	}


	public JTextField getTxtTotal(){
		return txtTotal;
	}
	public JTextField getTxtBuscar(){
		return txtBuscar;
	}
	public JTextField getTxtFecha(){
		return txtFecha;
	}
	
public void conectarContralador(CtlRequisicion c){
		
		
		
		tableDetalle.addKeyListener(c);
		tableDetalle.addMouseListener(c);
		modeloTabla.addTableModelListener(c);
		//tableDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableDetalle.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableDetalle.setColumnSelectionAllowed(true);
		tableDetalle.setRowSelectionAllowed(true);
		
		/*txtIdcliente.addKeyListener(c);
		txtNombrecliente.addKeyListener(c);
		txtFechafactura.addKeyListener(c);*/
		
		this.btnBuscar.addKeyListener(c);
		this.btnBuscar.addActionListener(c);
		this.btnBuscar.setActionCommand("BUSCARARTICULO");
		txtBuscar.addKeyListener(c);
		txtBuscar.addActionListener(c);
		txtBuscar.setActionCommand("BUSCARARTICULO2");
		
		cbxDepatOrigen.addItemListener(c);
		
		this.btnCerrar.addKeyListener(c);
		this.btnCerrar.addActionListener(c);
		this.btnCerrar.setActionCommand("CERRAR");
		
		/*this.btnCliente.addKeyListener(c);
		this.btnCliente.addActionListener(c);
		this.btnCliente.setActionCommand("BUSCARCLIENTES");
		
		this.btnCobrar.addKeyListener(c);
		this.btnCobrar.addActionListener(c);
		this.btnCobrar.setActionCommand("COBRAR");*/
		
		this.btnGuardar.addKeyListener(c);
		this.btnGuardar.addActionListener(c);
		this.btnGuardar.setActionCommand("GUARDAR");
		
		/*btnActualizar.addKeyListener(c);
		this.btnActualizar.addActionListener(c);
		this.btnActualizar.setActionCommand("ACTUALIZAR");
		
		this.rdbtnContado.addKeyListener(c);
		this.rdbtnCredito.addKeyListener(c);
		this.txtDescuento.addKeyListener(c);
		this.txtImpuesto.addKeyListener(c);
		this.txtSubtotal.addKeyListener(c);
		this.txtTotal.addKeyListener(c);
		
		//txtBuscar.
		txtArticulo.addKeyListener(c);
		txtPrecio.addKeyListener(c);*/
		//KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		//manager.addKeyEventDispatcher( c);
		//this.addWindowListener(c);
		//this.addw
	}
}
