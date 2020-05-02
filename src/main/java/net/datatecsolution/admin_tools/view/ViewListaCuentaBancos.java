package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlBancosLista;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.TmBanco;

import javax.swing.*;
import java.awt.*;

public class ViewListaCuentaBancos extends ViewTabla {
	
	private TmBanco modelo;
	protected JButton btnLimpiar;
	
	private JRadioButton rdbtnNombre;
	private JRadioButton rdbtnNoCuenta;

	public ViewListaCuentaBancos(Window view) {
		super(view, "Cuentas de Bancos");
		// TODO Auto-generated constructor stub
		
		
		rdbtnNoCuenta = new JRadioButton("# Cuenta",false);
		panelOpcioneBusqueda.add(rdbtnNoCuenta);
		grupoOpciones.add(rdbtnNoCuenta);
		
		rdbtnNombre = new JRadioButton("Nombre",false);
		panelOpcioneBusqueda.add(rdbtnNombre);
		grupoOpciones.add(rdbtnNombre);
		
		btnLimpiar = new BotonLimpiar();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
		//tabla y sus componentes
		modelo=new TmBanco();
		
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

	/**
	 * @return the modelo
	 */
	public TmBanco getModelo() {
		return modelo;
	}

	/**
	 * @return the rdbtnNombre
	 */
	public JRadioButton getRdbtnNombre() {
		return rdbtnNombre;
	}

	/**
	 * @return the rdbtnNoCuenta
	 */
	public JRadioButton getRdbtnNoCuenta() {
		return rdbtnNoCuenta;
	}
	public void conectarCtl( CtlBancosLista c){
		
		
		rdbtnNombre.addActionListener(c);
		rdbtnNombre.setActionCommand("ESCRIBIR");
		
		rdbtnNoCuenta.addActionListener(c);
		rdbtnNoCuenta.setActionCommand("ESCRIBIR");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("FECHA");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
	
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ELIMINAR");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

}
