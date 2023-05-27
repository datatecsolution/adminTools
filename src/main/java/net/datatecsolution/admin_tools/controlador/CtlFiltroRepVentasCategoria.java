package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepVentasArticulo;
import net.datatecsolution.admin_tools.view.ViewFiltroRepVentasCategoria;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CtlFiltroRepVentasCategoria implements ActionListener, KeyListener  {
	private ViewFiltroRepVentasCategoria view;
	private Categoria categoriaReporte=null;
	private DetalleFacturaDao detalleFacturaDao;

	private CajaDao cajasDao;
	private List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();

	private List<Caja> listCajas=null;
	private String date1;
	private String date2;
	public CtlFiltroRepVentasCategoria(ViewFiltroRepVentasCategoria v, Categoria a){
		
		view =v;
		view.conectarCtl(this);

		categoriaReporte=a;
		cajasDao=new CajaDao() ;
		detalleFacturaDao=new DetalleFacturaDao();
		listCajas=cajasDao.todosList();
		view.getTxtArticulo().setText(a.getDescripcion());
		
		
		
		
		Date horaLocal=new Date();
		view.getBuscar1().setDate(horaLocal);
		view.getBuscar2().setDate(horaLocal);
		
		view.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":
				
					Caja una=null;
					detalles.clear();
					for(int x=0;x<this.listCajas.size();x++){
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						date1 = sdf.format(this.view.getBuscar1().getDate());
						date2 = sdf.format(this.view.getBuscar2().getDate());

						//para volcar los detalles de una factura determinada
						List<DetalleFactura> unDetalle=new ArrayList<DetalleFactura>();

						detalleFacturaDao.getDetallesFactura(listCajas.get(x), unDetalle, categoriaReporte, date1, date2);

						//se vuelcan los datos en el detalle condesado donde solo se incluye una vez el producto
						//recorremos las detalles de la factura
						for(int xx=0; xx<unDetalle.size();xx++){

							int filaDetalleEncontra=-1;
							//se extre el articulo del detalle
							Articulo unArt=unDetalle.get(xx).getArticulo();

							//se recorre los detalles del informe condesando en busca del mismo articulo
							for(int yy=0;yy<detalles.size();yy++){

								if(unArt.getId()==detalles.get(yy).getArticulo().getId()){
									filaDetalleEncontra=yy;
									break;
								}
							}

							//se filtra si ya esta
							if(filaDetalleEncontra==-1){//sino esta el articulo en el reporte se agrega normal
								detalles.add(unDetalle.get(xx));
							}else{//si existe el articulo se suma la cantidad y el total a ya existente
								BigDecimal newCantida=detalles.get(filaDetalleEncontra).getCantidad().add(unDetalle.get(xx).getCantidad());
								BigDecimal newTotal=detalles.get(filaDetalleEncontra).getTotal().add(unDetalle.get(xx).getTotal());
								detalles.get(filaDetalleEncontra).setCantidad(newCantida);
								detalles.get(filaDetalleEncontra).setTotal(newTotal);

							}

						}







						una=listCajas.get(x);
					}
					try {
						
						AbstractJasperReports.createReportVentasCategoria(ConexionStatic.getPoolConexion().getConnection(),detalles,categoriaReporte,date1,date2);
						
						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
					} catch (SQLException eee) {
						// TODO Auto-generated catch block
						eee.printStackTrace();
					}
					
			break;
		}
		
	
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch(e.getKeyCode()){
		
		case KeyEvent.VK_F1:
			//buscarEmpleado();
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	

}
