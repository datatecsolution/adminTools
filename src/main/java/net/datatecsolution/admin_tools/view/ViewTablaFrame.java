package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.view.botones.BotonAgregar;
import net.datatecsolution.admin_tools.view.botones.BotonBuscar;
import net.datatecsolution.admin_tools.view.botones.BotonEliminar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ViewTablaFrame extends JInternalFrame {
	
	protected BorderLayout miEsquema;
	protected GridLayout miEsquemaTabla;
	
	protected JPanel panelAccion;
	protected JPanel panelSuperior;
	protected JPanel panelBusqueda;
	protected JPanel panelPaginacion;
	
	
	protected BotonAgregar btnAgregar;
	protected BotonEliminar btnEliminar;
	protected JTextField txtBuscar;
	//protected JTextField txtBuscar;
	private JTextField txtBuscar2;
	
	
	protected JRadioButton rdbtnId;
	protected ButtonGroup grupoOpciones; // grupo de botones que contiene los botones de opción
	protected JRadioButton rdbtnTodos;
	protected JRadioButton rdbtnFecha;
	
	protected JButton btnSiguiente;
	protected JButton btnAnterior;
	protected JTextField txtPagina;
	protected JScrollPane scrollPane;
	
	protected JTable tabla;
	protected JPanel panelOpcioneBusqueda;
	
	
	protected PanelPadre panelFechas;



	protected JDateChooser dcFecha1;



	protected JDateChooser dcFecha2;



	private JLabel label;



	private JLabel label_1;
	protected PanelPadre panel;
	protected BotonBuscar btnBuscar;
	private Dimension dim;
	
	private int anchoVentana=800;
	private int altoVentana=565;
	
	public ViewTablaFrame(String string, boolean b, boolean c, boolean d, boolean e){
		super( string,  b,  c,  d,  e );
		miEsquema=new BorderLayout();
		//this.setTitle("Articulos");
		getContentPane().setLayout(miEsquema);
		
		
		
		//creacion de los paneles
		panelAccion=new PanelPadre();
		FlowLayout flowLayout_1 = (FlowLayout) panelAccion.getLayout();
		flowLayout_1.setHgap(1);
		panelBusqueda=new PanelPadre();
		panelSuperior=new PanelPadre();
		FlowLayout flowLayout = (FlowLayout) panelSuperior.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelPaginacion=new PanelPadre();
		scrollPane = new JScrollPane();
		
		panelAccion.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Acciones de registro", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelBusqueda.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Busqueda de registros", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		//agregar componentes al panel acciones
		btnAgregar = new BotonAgregar();
		btnAgregar.setMnemonic('r');
		panelAccion.add(btnAgregar);
       
		btnEliminar = new BotonEliminar();
        btnEliminar.setEnabled(false);
        panelAccion.add(btnEliminar);
        
        
        //configuracion del panel busqueda
        grupoOpciones = new ButtonGroup(); // crea ButtonGroup
        
        panelOpcioneBusqueda = new PanelPadre();
        FlowLayout flowLayout_2 = (FlowLayout) panelOpcioneBusqueda.getLayout();
        flowLayout_2.setHgap(2);
        panelBusqueda.add(panelOpcioneBusqueda);
        rdbtnTodos = new JRadioButton("Todos");
        panelOpcioneBusqueda.add(rdbtnTodos);
		rdbtnTodos.setSelected(true);
		grupoOpciones.add(rdbtnTodos);
		rdbtnTodos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTamanioVentana(1);
			}
		});
		
		//opciones de busquedas
		rdbtnId = new JRadioButton("ID",false);
		panelOpcioneBusqueda.add(rdbtnId);
		rdbtnId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTamanioVentana(1);
			}
		});
		grupoOpciones.add(rdbtnId);
		
		rdbtnFecha = new JRadioButton("Fecha",false);
		panelOpcioneBusqueda.add(rdbtnFecha);
		grupoOpciones.add(rdbtnFecha);
		rdbtnFecha.setVisible(false);
		rdbtnFecha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTamanioVentana(2);
			}
		});
		
		
		panel = new PanelPadre();
		panelBusqueda.add(panel);
		//elementos del panel buscar
		txtBuscar=new JTextField(10);
		panel.add(txtBuscar);
		
		txtBuscar2 = new JTextField();
		panel.add(txtBuscar2);
		txtBuscar2.setEditable(false);
		txtBuscar2.setVisible(false);
		txtBuscar2.setEditable(false);
		txtBuscar2.setColumns(10);
		
		
		panelFechas = new PanelPadre();
		FlowLayout flowLayout_3 = (FlowLayout) panelFechas.getLayout();
		flowLayout_3.setHgap(2);
		panelBusqueda.add(panelFechas);
		
		label = new JLabel("de");
		panelFechas.add(label);
		
		dcFecha1 = new JDateChooser();
		panelFechas.add(dcFecha1);
		dcFecha1.setSize(new Dimension(100, 20));
		dcFecha1.setPreferredSize(new Dimension(160, 27));
		dcFecha1.setDateFormatString("dd-MM-yyyy");
		
		label_1 = new JLabel("hasta");
		panelFechas.add(label_1);
		
		dcFecha2 = new JDateChooser();
		panelFechas.add(dcFecha2);
		dcFecha2.setSize(new Dimension(100, 20));
		dcFecha2.setPreferredSize(new Dimension(160, 27));
		dcFecha2.setDateFormatString("dd-MM-yyyy");
		panelFechas.setVisible(false);
		
		
		
		btnBuscar=new BotonBuscar();
		panelBusqueda.add(btnBuscar);
		
		
		//la tabla de los elementos
		tabla=new JTable();
		tabla.setRowHeight(20);
		//se agrega la tabla a scrollPanel
		//scrollPane.setBounds(36, 97, 742, 136);
		scrollPane.setViewportView(tabla);
        
     
		
		
		//configuracion de los paneles
		panelSuperior.add(panelAccion);
		panelSuperior.add(panelBusqueda);
		getContentPane().add(panelSuperior, BorderLayout.NORTH);
		
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		
		getContentPane().add(panelPaginacion, BorderLayout.SOUTH);
		panelPaginacion.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnAnterior = new JButton("Anterior");
		panelPaginacion.add(btnAnterior);
		
		txtPagina = new JTextField();
		txtPagina.setEditable(false);
		txtPagina.setHorizontalAlignment(SwingConstants.CENTER);
		txtPagina.setText("1");
		panelPaginacion.add(txtPagina);
		txtPagina.setColumns(4);
		
		btnSiguiente = new JButton("Siguiente");
		panelPaginacion.add(btnSiguiente);
		
		this.setResizable(false);
		
		dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setTamanioVentana(1);
		
		
		
	}
	public void setTamanioVentana(int i) {
		// TODO Auto-generated method stub
		if(i==1){
			
			
			panel.setVisible(true);
			panelFechas.setVisible(false);
			setSize(790,565);
			txtBuscar.selectAll();
			txtBuscar.requestFocusInWindow();
			
		}else
			if(i==2){
				panel.setVisible(false);
				panelFechas.setVisible(true);
				setSize(950,565);
			
			}
		
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
	}
	public JTextField getTxtPagina(){
		return txtPagina;
	}
	
	public JButton getBtnEliminar(){
		return btnEliminar;
	}
	public JRadioButton getRdbtnId(){
		return rdbtnId;
	}
	public JRadioButton getRdbtnTodos(){
		return rdbtnTodos;
		
	}
	public BotonAgregar getBtnAgregar(){
		return btnAgregar;
	}
	
	public JTable getTabla(){
		return tabla;
	}
	/**
	 * @return the dcFecha1
	 */
	public JDateChooser getDcFecha1() {
		return dcFecha1;
	}


	/**
	 * @return the dcFecha2
	 */
	public JDateChooser getDcFecha2() {
		return dcFecha2;
	}


	/**
	 * @return the panelFechas
	 */
	public PanelPadre getPanelFechas() {
		return panelFechas;
	}
	/**
	 * @return the panel
	 */
	public JPanel getPanelBusqueda() {
		return panel;
	}
	public JTextField getTxtBuscar(){
		return txtBuscar;
	}
	public JTextField getTxtBuscar2(){
		return txtBuscar2;
	}
	
	public JRadioButton getRdbtnFecha(){
		return rdbtnFecha;
	}
	


}
