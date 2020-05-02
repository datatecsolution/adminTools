package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.modelo.dao.UsuarioDao;
import net.datatecsolution.admin_tools.view.ViewFiltroComisiones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CtlFiltroRepVentasUsuarios implements ActionListener {
	
	
	private ViewFiltroComisiones view;
	private CajaDao cajasDao;
	private FacturaDao facturaDao;
	private UsuarioDao usuarioDao;
	private List<Comision> ventas= new ArrayList<Comision>();
	private List<Empleado> empleados;

	public CtlFiltroRepVentasUsuarios(ViewFiltroComisiones v) {
		// TODO Auto-generated constructor stub
		view =v;
		view.setTitle("Reporte de ventas por usuarios");
		cajasDao=new CajaDao();
		facturaDao=new FacturaDao();
		usuarioDao=new UsuarioDao();
		usuarioDao.setEstado(2);
		view.conectarCtl(this);
		agregarUsuarios(usuarioDao.todos());
		Date horaLocal=new Date();
		view.getBuscar1().setDate(horaLocal);
		view.getBuscar2().setDate(horaLocal);
		view.setVisible(true);
	}

	private void agregarUsuarios(List<Usuario> usuarios) {
		// TODO Auto-generated method stub
		if(usuarios!=null){
			for(int x=0; x<usuarios.size();x++){
				
				if(usuarios.get(x).getTipoPermiso()==2){
					Comision un=new Comision();
					un.setCodigoVendedor(usuarios.get(x).getCodigo());
					un.setVendedorNombre(usuarios.get(x).getNombre()+" "+usuarios.get(x).getApellido());
					ventas.add(un);
				}
				
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getBuscar1().getDate());
				String date2 = sdf.format(this.view.getBuscar2().getDate());
				
				List<Caja> cajas=cajasDao.todosList();
				if(cajas!=null || cajas.isEmpty())
				{
					for(int x=0;x<cajas.size();x++){
						//JOptionPane.showMessageDialog(view, cajas.get(x).toString()+ " |||| item: "+x);
						facturaDao.getVentasUsuarios(date1,date2,cajas.get(x),ventas,view.getModelJs().getNumber().intValue());
						
						
					}
					
					
					try {
						
						//AbstractJasperReports.createReportComisiones(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(), view.getModelJs().getNumber().intValue());
						AbstractJasperReports.createReportVentasUsuarios(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(),view.getBuscar2().getDate(), ventas,view.getModelJs().getNumber().intValue());
						
						AbstractJasperReports.showViewer(view);
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					//se limpian los totales para un proximo reporte
					for(int y=0;y<ventas.size();y++){
						ventas.get(y).resetTotales();
					}
					
					
				}
				
				
				
			
					
					
			break;
		}
		
	}

}
