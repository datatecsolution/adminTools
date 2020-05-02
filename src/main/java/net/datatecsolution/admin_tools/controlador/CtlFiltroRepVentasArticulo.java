package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepVentasArticulo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CtlFiltroRepVentasArticulo implements ActionListener, KeyListener  {
	private ViewFiltroRepVentasArticulo view;
	private Articulo articuloReporte=null;
	private DetalleFacturaDao detalleFacturaDao;
	
	private CajaDao cajasDao;
	private List<DetalleFactura> detalles=new ArrayList<DetalleFactura>();
	
	private List<Caja> listCajas=null;
	private String date1;
	private String date2;
	public CtlFiltroRepVentasArticulo(ViewFiltroRepVentasArticulo v, Articulo a){
		
		view =v;
		view.conectarCtl(this);

		articuloReporte=a;
		cajasDao=new CajaDao() ;
		detalleFacturaDao=new DetalleFacturaDao();
		listCajas=cajasDao.todosList();
		view.getTxtArticulo().setText(a.getArticulo());
		
		
		
		
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
						
						detalleFacturaDao.getDetallesFactura(listCajas.get(x), detalles, articuloReporte, date1, date2);
						una=listCajas.get(x);
					}
					try {
						
						AbstractJasperReports.createReportVentasArticulo(ConexionStatic.getPoolConexion().getConnection(),detalles,articuloReporte,date1,date2);
						
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
