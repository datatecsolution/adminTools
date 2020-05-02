package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;
import net.datatecsolution.admin_tools.modelo.dao.CierreFacturacionDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepCierreVentaDetalleCateg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CtlFiltroRepCierreVentasDetalleCateg implements ActionListener {
	
	private ViewFiltroRepCierreVentaDetalleCateg view;
	private CategoriaDao categoriaDao;
	private CierreCaja elCierre=null;
	private DetalleFacturaDao detalleFacturaDao=new DetalleFacturaDao();

	public CtlFiltroRepCierreVentasDetalleCateg(ViewFiltroRepCierreVentaDetalleCateg v,CierreCaja c) {
		// TODO Auto-generated constructor stub
		
		view =v;
		elCierre=c;
		view.conectarCtl(this);
		
		categoriaDao=new CategoriaDao();
		cargarComboBox();
		
		
		view.setVisible(true);
	}
	
	private void cargarComboBox(){
		//se crea el objeto para obtener de la bd los impuestos
		//myImpuestoDao=new ImpuestoDao(conexion);
	
		//se obtiene la lista de los impuesto y se le pasa al modelo de la lista
		this.view.getModeloListaCategorias().setLista(this.categoriaDao.todosVector());
		
		
		//se remueve la lista por defecto
		this.view.getCbxCategorias().removeAllItems();
	
		this.view.getCbxCategorias().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GENERAR":
			//se recoge la categoria
			Categoria myCategoria=(Categoria)view.getCbxCategorias().getSelectedItem();
			
		
	        
	        
	        //si seleccion una fila
	        if(elCierre!=null){
	        	
	        	
	        	
				
				//se crea el informe de ventas por categorias
				List<DetalleFactura> ventas= new ArrayList<DetalleFactura>();
				
				
				//se consigue los registros de las facturas de los cierre
				CierreFacturacionDao cierreFacturacioDao=new CierreFacturacionDao();
				elCierre.setCierreFacturas(cierreFacturacioDao.buscarIdCierre(elCierre.getId()));
				
				//si existe los registros de los cierres se recororen en busca los totales en las facturas
				if(elCierre.getCierreFacturas()!=null){
					
					//se recorren los registros para sacar los totales de la facturas
					for(int xx=0;xx<elCierre.getCierreFacturas().size();xx++){
						
						
							detalleFacturaDao.getDetallesFacturaCategoria(elCierre.getCierreFacturas().get(xx), 
															elCierre.getUsuario(), 
															myCategoria, ventas);
						
						
				
					}
					
					try {
					
						AbstractJasperReports.createRepCierreVentasDetallesCateg(ConexionStatic.getPoolConexion().getConnection(),elCierre,ventas,myCategoria);
						
						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
					} catch (SQLException eee) {
						// TODO Auto-generated catch block
						eee.printStackTrace();
					}
				}
	        }

			
			break;
		}
	}

}
