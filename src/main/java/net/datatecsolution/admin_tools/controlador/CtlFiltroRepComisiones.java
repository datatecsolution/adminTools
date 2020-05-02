package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroComisiones;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class CtlFiltroRepComisiones implements ActionListener {
	
	
	private ViewFiltroComisiones view;
	private CajaDao cajasDao;
	private FacturaDao facturaDao;
	private EmpleadoDao empleadoDao;
	private List<Comision> comisiones= new ArrayList<Comision>();
	private List<Empleado> empleados;

	public CtlFiltroRepComisiones(ViewFiltroComisiones v) {
		// TODO Auto-generated constructor stub
		view =v;
		cajasDao=new CajaDao();
		facturaDao=new FacturaDao();
		empleadoDao=new EmpleadoDao();
		empleadoDao.setEstado(2);
		view.conectarCtl(this);
		
		Date horaLocal=new Date();
		view.getBuscar1().setDate(horaLocal);
		view.getBuscar2().setDate(horaLocal);
		view.setVisible(true);
	}

	private void agregarEmpleado(Vector<Empleado> empleados) {
		// TODO Auto-generated method stub
		if(empleados!=null){
			comisiones.clear();
			for(int x=0; x<empleados.size();x++){
				Comision un=new Comision();
				un.setCodigoVendedor(empleados.get(x).getCodigo());
				un.setVendedorNombre(empleados.get(x).getNombre()+" "+empleados.get(x).getApellido());
				comisiones.add(un);
				
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":
				agregarEmpleado(empleadoDao.todoEmpleadosVendedores());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String date1 = sdf.format(this.view.getBuscar1().getDate());
				String date2 = sdf.format(this.view.getBuscar2().getDate());
				
				List<Caja> cajas=cajasDao.todosList();
				if(cajas!=null || cajas.isEmpty())
				{
					for(int x=0;x<cajas.size();x++){
						//JOptionPane.showMessageDialog(view, cajas.get(x).toString()+ " |||| item: "+x);
						facturaDao.getComiciones(date1,date2,cajas.get(x),comisiones,view.getModelJs().getNumber().intValue());
						
						
					}
					
					
					try {
						
						//AbstractJasperReports.createReportComisiones(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(), view.getModelJs().getNumber().intValue());
						AbstractJasperReports.createReportComisiones(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(),view.getBuscar2().getDate(), comisiones,view.getModelJs().getNumber().intValue());
						
						AbstractJasperReports.showViewer(view);
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					//se limpian los totales para un proximo reporte
					for(int y=0;y<comisiones.size();y++){
						comisiones.get(y).resetTotales();
					}
					
					
				}
				
				
				
			
					
					
			break;
		}
		
	}

}
