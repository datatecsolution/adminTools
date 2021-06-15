package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.view.ViewFiltroPagos;
import net.datatecsolution.admin_tools.view.ViewListaClientes;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Date;

public class CtlFiltroRepPagosVendedor implements ActionListener, KeyListener  {
	private ViewFiltroPagos view;
	private Cliente myCliente=null;
	private ClienteDao myClienteDao;
	private Empleado myVendedor=null;
	private EmpleadoDao myEmpleadoDao;


	public CtlFiltroRepPagosVendedor(ViewFiltroPagos v){
		
		view =v;
		view.conectarCtl(this);
		myEmpleadoDao=new EmpleadoDao();

		//se busca el empleado por defecto es con el id 0
		myVendedor=myEmpleadoDao.buscarPorId(1);
		if(myVendedor!=null){
			view.getTxtCliente().setText(myVendedor.toString());
		}
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
					
				
				if(myVendedor!=null)
				{
				
						try {
							
							AbstractJasperReports.createReportPagoClienteVendedor(ConexionStatic.getPoolConexion().getConnection(),view.getBuscar1().getDate(), view.getBuscar2().getDate(),myVendedor.getCodigo());
							
							AbstractJasperReports.showViewer(view);
						} catch (SQLException ee) {
							// TODO Auto-generated catch block
							ee.printStackTrace();
							
							JOptionPane.showMessageDialog(view, "No se puede mostrar el reporte.","Error de reporte",JOptionPane.ERROR_MESSAGE);
						}
				}else{
					JOptionPane.showMessageDialog(view, "Debe seleccionar un cliente priemero. Utilice F1 para buscar el cliente.","Error validacion.",JOptionPane.ERROR_MESSAGE);
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
			buscarEmpleado();
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void buscarCliente(){
		
		ViewListaClientes viewBuscarCliente=new ViewListaClientes(view);
		CtlClienteBuscar ctBuscarCliente=new CtlClienteBuscar(viewBuscarCliente);
		viewBuscarCliente.pack();
		
		boolean resultado=ctBuscarCliente.buscarCliente(view);
		
		if(resultado){
			myCliente=ctBuscarCliente.getCliente();
			view.getTxtCliente().setText(myCliente.getNombre());
		}
		
	}
	public void buscarEmpleado(){
		ViewListaEmpleados viewBuscarEmpleado=new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado=new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado=ctBuscarEmpleado.buscar();

		if(resultado){
			myVendedor=ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtCliente().setText(myVendedor.toString());
		}else {
			myVendedor=null;
		}

	}

}
