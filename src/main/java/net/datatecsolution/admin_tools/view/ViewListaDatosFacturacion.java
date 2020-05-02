package net.datatecsolution.admin_tools.view;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;

import net.datatecsolution.admin_tools.controlador.CtlDatosFacturacionLista;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.view.botones.BotonLimpiar;
import net.datatecsolution.admin_tools.view.rendes.TablaRenderizadorProveedor;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCajas;
import net.datatecsolution.admin_tools.view.tablemodel.TmDatosFacturacion;

public class ViewListaDatosFacturacion extends ViewTabla {
	
	private TmDatosFacturacion modelo;
	private JComboBox<Caja> cbxCajas;
	protected JButton btnLimpiar;
	private CbxTmCajas modeloListaCajas;

	public ViewListaDatosFacturacion(Window view) {
		super(view, "Datos de facturacion");
		// TODO Auto-generated constructor stub
		 btnLimpiar = new BotonLimpiar();
	        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
	    panelAccion.add(btnLimpiar);
	    
	    super.btnEliminar.setEnabled(true);
		
		modeloListaCajas=new CbxTmCajas();//comentar para poder mostrar en forma de diseno la ventana
		modeloListaCajas.agregar(new Caja());
		
		cbxCajas = new JComboBox<Caja>(modeloListaCajas);
		panelOpcioneBusqueda.add(cbxCajas);
		
		modelo=new TmDatosFacturacion();
		
		tabla.setModel(modelo);
		TablaRenderizadorProveedor renderizador = new TablaRenderizadorProveedor();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(5);     //Tamaño de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(3).setPreferredWidth(100);		//
		tabla.getColumnModel().getColumn(4).setPreferredWidth(100);	
		tabla.getColumnModel().getColumn(5).setPreferredWidth(100);	
		tabla.getColumnModel().getColumn(6).setPreferredWidth(100);	
	}

	/**
	 * @return the modelo
	 */
	public TmDatosFacturacion getModelo() {
		return modelo;
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
	public void conectarControlador(CtlDatosFacturacionLista c){
		
		cbxCajas.addActionListener(c);
		cbxCajas.setActionCommand("CAMBIOCOMBOBOX");
		
		rdbtnId.addActionListener(c);
		rdbtnId.setActionCommand("ID");
		
		
		
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
