package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlFacturasCompra;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturaCompra;
import net.datatecsolution.admin_tools.view.tablemodel.TablaModeloFacturasCompra;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewListaFacturasCompra extends ViewTabla {
	
	
	protected BotonImprimirSmall btnImprimir;
	
	private TablaModeloFacturasCompra modelo;
	
	private JRadioButton rdbtnProveedor;


	public ViewListaFacturasCompra(JFrame view) {
		super(view,"Facturas compras");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
		
		rdbtnProveedor = new JRadioButton("Proveedor");
		panelOpcioneBusqueda.add(rdbtnProveedor);
		grupoOpciones.add(rdbtnProveedor);	
		
		
		
		rdbtnFecha.setVisible(true);
		
	    
	    btnImprimir = new BotonImprimirSmall();
	    btnImprimir.setEnabled(false);
	    //btnLimpiar.setIcon(new ImageIcon("recursos/clear.png")); // NOI18N
	    panelAccion.add(btnImprimir);
		
		
	    
	    //tabla y sus componentes
		modelo=new TablaModeloFacturasCompra();
		
		tabla.setModel(modelo);
		RenderizadorTablaFacturaCompra renderizador = new RenderizadorTablaFacturaCompra();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(60);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(80);	//de las columnas
		tabla.getColumnModel().getColumn(2).setPreferredWidth(70);	//en la tabla
		tabla.getColumnModel().getColumn(3).setPreferredWidth(280);	//
		
	}
	
public void conectarControlador(CtlFacturasCompra c){
		
		rdbtnTodos.addActionListener(c);
		rdbtnTodos.setActionCommand("TODAS");
		
		rdbtnId.addActionListener(c);
		//rdbtnId.getActionCommand();
		rdbtnId.setActionCommand("ID");
		
		rdbtnFecha.addActionListener(c);
		rdbtnFecha.setActionCommand("FECHA");
		
		rdbtnProveedor.addActionListener(c);
		rdbtnProveedor.setActionCommand("ESCRIBIR");
		
		
		
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		
		 btnAgregar.addActionListener(c);
		 btnAgregar.setActionCommand("INSERTAR");
		 
		 
		 btnEliminar.addActionListener(c);
		 btnEliminar.setActionCommand("ANULARFACTURA");
		 
		 btnImprimir.addActionListener(c);
		 btnImprimir.setActionCommand("IMPRIMIR");
		 
		 txtBuscar.addActionListener(c);
		 txtBuscar.setActionCommand("BUSCAR");
		 
		 
		 
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 tabla.addMouseListener(c);
		 tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

public JTextField getTxtPagina(){
	return txtPagina;
}
	
	
	public TablaModeloFacturasCompra getModelo(){
		return modelo;
	}
	public JButton getBtnEliminar(){
		return btnEliminar;
	}
	
	public BotonImprimirSmall getBtnImprimir(){
		return btnImprimir;
	}
	public JRadioButton getRdbtnProveedor() {
		return rdbtnProveedor;
	}

	public void setRdbtnProveedor(JRadioButton rdbtnProveedor) {
		this.rdbtnProveedor = rdbtnProveedor;
	}
	


}
