package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlProgramarPrecios;
import net.datatecsolution.admin_tools.view.botones.BotonAgregar;
import net.datatecsolution.admin_tools.view.botones.BotonEliminar;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFactura;
import net.datatecsolution.admin_tools.view.tablemodel.TmProgramarPrecio;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ViewListaPrecioProgramar  extends JDialog{
	protected BorderLayout miEsquema;
	protected GridLayout miEsquemaTabla;
	
	protected JPanel panelAccion;
	protected JPanel panelSuperior;
	
	
	protected BotonAgregar btnAgregar;
	protected BotonEliminar btnEliminar;
	protected BotonImprimirSmall btnImprimir;
	private ButtonGroup grupoOpciones; // grupo de botones que contiene los botones de opci�n
	
	
	
	private JTable tablaArticulo;
	
	private TmProgramarPrecio modelo;

	public ViewListaPrecioProgramar(Window view) {
		miEsquema=new BorderLayout();
		this.setTitle("Pagos de clientes");
		this.setLocationRelativeTo(view);
		this.setModal(true);
		getContentPane().setLayout(miEsquema);
		
		
		
		//creacion de los paneles
		panelAccion=new JPanel();
		panelSuperior=new JPanel();
		
		panelAccion.setBorder(new TitledBorder(new LineBorder(new Color(130, 135, 144)), "Acciones de registro", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		//agregar componentes al panel acciones
		btnAgregar = new BotonAgregar();
		//btnAgregar.setEnabled(false);
		btnAgregar.setMnemonic('r');
		panelAccion.add(btnAgregar);
	   
		btnEliminar = new BotonEliminar();
		btnEliminar.setToolTipText("Anular Facturas");
		btnEliminar.setEnabled(false);
	    panelAccion.add(btnEliminar);
	    
	    btnImprimir = new BotonImprimirSmall();
	    btnImprimir.setEnabled(false);
	    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
	    panelAccion.add(btnImprimir);
	    //panelAccion.setVisible(false);	
	    
	    //configuracion del panel busqueda
	    grupoOpciones = new ButtonGroup();
        //tabla y sus componentes
		modelo=new TmProgramarPrecio();
		tablaArticulo=new JTable();
		tablaArticulo.setModel(modelo);
		RenderizadorTablaFactura renderizador = new RenderizadorTablaFactura();
		tablaArticulo.setDefaultRenderer(String.class, renderizador);
		
		tablaArticulo.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tama�o de las columnas de las tablas
		tablaArticulo.getColumnModel().getColumn(1).setPreferredWidth(200);	//
		tablaArticulo.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tablaArticulo.getColumnModel().getColumn(3).setPreferredWidth(10);	//
		
		
		JScrollPane scrollPane = new JScrollPane(tablaArticulo);
		scrollPane.setBounds(36, 97, 742, 136);
		
		
		//configuracion de los paneles
		panelSuperior.add(panelAccion);
		getContentPane().add(panelSuperior, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		setSize(729,600);
		
		//se hace visible
		//setVisible(true);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	public JTable getTabla(){
		return tablaArticulo;
	}
	public TmProgramarPrecio getModelo(){
		return modelo;
	}
	public void conectarControlador(CtlProgramarPrecios c){
		this.addWindowListener(c);
		
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
		 
				 
		 tablaArticulo.addMouseListener(c);
		 tablaArticulo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

}
