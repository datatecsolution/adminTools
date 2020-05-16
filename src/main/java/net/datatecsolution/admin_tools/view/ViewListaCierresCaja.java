package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlCierresCajaLista;
import net.datatecsolution.admin_tools.view.botones.BotonImprimirSmall;
import net.datatecsolution.admin_tools.view.botones.BotonReporte;
import net.datatecsolution.admin_tools.view.rendes.RenderizadorTablaFacturaCompra;
import net.datatecsolution.admin_tools.view.tablemodel.TmCierres;

import javax.swing.*;
import java.awt.*;

public class ViewListaCierresCaja extends ViewTabla {
	
	
	protected JButton btnLimpiar;
	
	
	
	private TmCierres modelo;
	private BotonReporte btnReporte;
	private JPopupMenu menuContextual; // permite al usuario seleccionar el color
	private JMenuItem mntmReporteVentaCategoria;
	private JMenuItem mntmReporteVentaDetallado;
	private JMenuItem mntmRepVentaDetalleCategoria;
	
	public ViewListaCierresCaja(Window view) {
		// TODO Auto-generated constructor stub
		super(view,"Cierres de caja");
		Init();
	}
	public void Init() {
		
menuContextual = new JPopupMenu(); // crea el men� contextual
		
		//opcion del menu flotante
		mntmReporteVentaCategoria = new JMenuItem("Reporte venta categoria");
		menuContextual.add(mntmReporteVentaCategoria);
		
		mntmRepVentaDetalleCategoria =new JMenuItem("Reporte venta categoria detallado");
		menuContextual.add(mntmRepVentaDetalleCategoria);
		
		mntmReporteVentaDetallado= new JMenuItem("Reporte venta detallado por caja");
		menuContextual.add(mntmReporteVentaDetallado);
		
		
	
        
        btnLimpiar = new BotonImprimirSmall();
        //btnLimpiar.setIcon(new ImageIcon(ViewListaMarca.class.getResource("/View/imagen/clear.png"))); // NOI18N
        panelAccion.add(btnLimpiar);
        
        btnReporte=new BotonReporte();
        panelAccion.add(btnReporte);
        
        rdbtnFecha.setVisible(true);
		
		//tabla y sus componentes
		modelo=new TmCierres();
		
		tabla.setModel(modelo);
		RenderizadorTablaFacturaCompra renderizador = new RenderizadorTablaFacturaCompra();
		tabla.setDefaultRenderer(String.class, renderizador);
		
		
		tabla.getColumnModel().getColumn(0).setPreferredWidth(100);     //Tama�o de las columnas de las tablas
		tabla.getColumnModel().getColumn(1).setPreferredWidth(100);	//
		tabla.getColumnModel().getColumn(2).setPreferredWidth(100);	//
		tabla.setAutoCreateRowSorter(true);
		//setSize(900,591);
		
		this.btnEliminar.setEnabled(false);
		//se hace visible
		//setVisible(true);
		
		
		
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	
	}
	
	public TmCierres getModelo(){
		return this.modelo;
	}
	
	
	public void conectarCtl(CtlCierresCajaLista c){
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("INSERTAR");
		
		btnEliminar.addActionListener(c);
		btnEliminar.setActionCommand("ELIMINAR");
		
		btnLimpiar.addActionListener(c);
		btnLimpiar.setActionCommand("IMPRIMIR");
		
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("BUSCAR");
		
		 btnSiguiente.addActionListener(c);
		 btnSiguiente.setActionCommand("NEXT");
		 
		 btnAnterior.addActionListener(c);
		 btnAnterior.setActionCommand("LAST");
		 
		 btnReporte.addActionListener(c);
		 btnReporte.setActionCommand("REPORTE_DETALLE_VENTA");
		 
		 
		 mntmReporteVentaCategoria.addActionListener(c);
		 mntmReporteVentaCategoria.setActionCommand("REPORTE_VENTA_CATEGORIA");
		 
		 mntmReporteVentaDetallado.addActionListener(c);
		 mntmReporteVentaDetallado.setActionCommand("REPORTE_DETALLE_VENTA");
		 
		 mntmRepVentaDetalleCategoria.addActionListener(c);
		 mntmRepVentaDetalleCategoria.setActionCommand("REPORTE_DETALLE_VENTA_CATEGORIA");
		
		
		
		tabla.addMouseListener(c);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	}
	
	public JPopupMenu getMenuContextual(){
		return menuContextual;
		
	}

}
