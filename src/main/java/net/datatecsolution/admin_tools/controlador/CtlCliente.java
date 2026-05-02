package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Cliente;
import net.datatecsolution.admin_tools.modelo.Empleado;
import net.datatecsolution.admin_tools.modelo.RutaCobro;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.EmpleadoDao;
import net.datatecsolution.admin_tools.modelo.dao.RutaCobroDao;
import net.datatecsolution.admin_tools.view.ViewCrearCliente;
import net.datatecsolution.admin_tools.view.ViewListaEmpleados;
import net.datatecsolution.admin_tools.view.ViewListaRutasCobro;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class CtlCliente implements ActionListener,WindowListener , KeyListener{

	private final ViewCrearCliente view;
	private Cliente myCliente=null;
	private ClienteDao myClienteDao=null;
	private boolean resultaOperacion=false;
	private Empleado myCobrador=null;
	private Empleado myVendedor=null;
	private RutaCobro myRuta=null;
	private final EmpleadoDao myEmpleadoDao;
	private RutaCobroDao myRutaDao;

	public CtlCliente(ViewCrearCliente v){
		view=v;
		view.conectarControlador(this);


		myEmpleadoDao=new EmpleadoDao();
		myRutaDao=new RutaCobroDao();

		//se busca el empleado por defecto es con el id 1 (cobrador y vendedor)
		myCobrador=myEmpleadoDao.buscarPorId(1);
		if(myCobrador!=null){
			view.getTxtCobrador().setText(myCobrador.toString());
			view.getTxtIdCobrador().setText(myCobrador.getCodigo()+" ");
		}
		myVendedor=myEmpleadoDao.buscarPorId(1);
		if(myVendedor!=null){
			view.getTxtVendedor().setText(myVendedor.toString());
			view.getTxtIdVendedor().setText(myVendedor.getCodigo()+" ");
		}
		myRuta=myRutaDao.buscarPorId(1);
		if(myRuta!=null){
			view.getTxtRutaCobro().setText(myRuta.getDescripcion());
			view.getTxtIdRutaCobro().setText(myRuta.getCodigo()+"");
		}
		myCliente=new Cliente();
		myClienteDao=new ClienteDao();
		view.setLocationRelativeTo(view);
		view.setModal(true);
		//view.setVisible(true);

	}
	private void getCliente(){
		myCliente.setNombre(view.getTxtNombre().getText());
		myCliente.setDireccion(view.getTxtDireccion().getText());
		myCliente.setTelefono(view.getTxtTelefono().getText());
		myCliente.setCelular(view.getTxtMovil().getText());
		myCliente.setRtn(view.getTxtRtn().getText());
		myCliente.setCobrador(myCobrador);
		myCliente.setVendedor(myVendedor);
		myCliente.setRutaCobro(myRuta);
	}

	public boolean agregarCliente(){
		this.view.setVisible(true);
		return resultaOperacion;
	}

	/*<<<<<<<<<<<<<<<<<<<<<<<<< metodo que devuelve el cliente guardado >>>>>>>>>>>>>>>>>>>>>>>>>         */
	public Cliente getClienteGuardado(){
		return myCliente;


	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();

		//se filtra el comando para verificar que la accion se genero desde los botones de las facturas pendientes.
		if(AbstractJasperReports.isNumber(comando)){
			int codigoCliente=Integer.parseInt(comando);

			Cliente cliente=myClienteDao.buscarPorId(codigoCliente);

			actualizarCliente(cliente);

		}

		switch(comando){
			case "BUSCAR_RUTA":
				if(AbstractJasperReports.isNumber(view.getTxtIdRutaCobro().getText())){
					myRuta=myRutaDao.buscarPorId(Integer.parseInt(view.getTxtIdRutaCobro().getText()));
					if(myRuta==null){
						myRuta=myRutaDao.buscarPorId(1);
						view.getTxtIdRutaCobro().setText(myRuta.getCodigo()+"");
					}
					view.getTxtRutaCobro().setText(myRuta.getDescripcion());
				}
				break;

			case "BUSCAR_COBRADOR":
				if(AbstractJasperReports.isNumber(view.getTxtIdCobrador().getText())){
					myCobrador=myEmpleadoDao.buscarPorId(Integer.parseInt(view.getTxtIdCobrador().getText()));
					if(myCobrador==null){
						myCobrador=myEmpleadoDao.buscarPorId(1);
						view.getTxtIdCobrador().setText(myCobrador.getCodigo()+" ");
					}
					view.getTxtCobrador().setText(myCobrador.toString());
				}
				break;

			case "BUSCAR_VENDEDOR":
				if(AbstractJasperReports.isNumber(view.getTxtIdVendedor().getText())){
					myVendedor=myEmpleadoDao.buscarPorId(Integer.parseInt(view.getTxtIdVendedor().getText()));
					if(myVendedor==null){
						myVendedor=myEmpleadoDao.buscarPorId(1);
						view.getTxtIdVendedor().setText(myVendedor.getCodigo()+" ");
					}
					view.getTxtVendedor().setText(myVendedor.toString());
				}
				break;


			case "GUARDAR":
				getCliente();
				//se ejecuta la accion de guardar
				if(myClienteDao.registrar(myCliente)){
					JOptionPane.showMessageDialog(this.view, "Se ha registrado Exitosamente","Informacion",JOptionPane.INFORMATION_MESSAGE);
					myCliente.setId(myClienteDao.getIdClienteRegistrado());//se completa el proveedor guardado con el ID asignado por la BD
					resultaOperacion=true;
					this.view.setVisible(false);
				}else{
					JOptionPane.showMessageDialog(this.view, "No se Registro");
					resultaOperacion=false;
					this.view.setVisible(false);
				}
				break;
			case "CANCELAR":
				this.view.setVisible(false);
				break;
			case "ACTUALIZAR":
				getCliente();
					if(myClienteDao.actualizar(myCliente)){//se manda actualizar el cliente en la bd
						JOptionPane.showMessageDialog(view, "Se Actualizo el articulo");
						resultaOperacion=true;
						this.view.dispose();
					}
				break;
		}
	}


	public boolean actualizarCliente(Cliente cliente){
		//se carga la configuracionde la view articulo para la actulizacion
		this.view.configActualizar();


		//se establece el nombre de articulo en la view
		this.view.getTxtNombre().setText(cliente.getNombre());

		//se establece la marca en la view
		this.view.getTxtDireccion().setText(cliente.getDereccion());

		this.view.getTxtTelefono().setText(cliente.getTelefono());
		this.view.getTxtMovil().setText(cliente.getCelular());
		this.view.getTxtRtn().setText(cliente.getRtn());

		this.myCobrador=cliente.getCobrador();
		this.myVendedor=cliente.getVendedor();
		this.myRuta=cliente.getRutaCobro();

		if (cliente.getCobrador() != null) {
			view.getTxtIdCobrador().setText(cliente.getCobrador().getCodigo()+"");
			view.getTxtCobrador().setText(cliente.getCobrador().toString());
		} else {
			view.getTxtIdCobrador().setText("");
			view.getTxtCobrador().setText("");
		}

		if (cliente.getVendedor() != null) {
			view.getTxtIdVendedor().setText(cliente.getVendedor().getCodigo()+"");
			view.getTxtVendedor().setText(cliente.getVendedor().toString());
		} else {
			view.getTxtIdVendedor().setText("");
			view.getTxtVendedor().setText("");
		}

		if (cliente.getRutaCobro() != null) {
			view.getTxtIdRutaCobro().setText(cliente.getRutaCobro().getCodigo()+"");
			view.getTxtRutaCobro().setText(cliente.getRutaCobro().getDescripcion());
		} else {
			view.getTxtIdRutaCobro().setText("");
			view.getTxtRutaCobro().setText("");
		}



		//se establece el articulo de la clase this
		myCliente=cliente;



		// se hace visible la ventana modal
		this.view.setVisible(true);



		return this.resultaOperacion;
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		resultaOperacion=false;
		view.setVisible(false);
		view.dispose();
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

		resultaOperacion=false;
		view.setVisible(false);
		view.dispose();


	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

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
			buscarCobrador();
			break;
		case KeyEvent.VK_F2:
			buscarVendedor();
			break;
		case KeyEvent.VK_F3:
			buscarRuta();
			break;
	}

	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

		view.getMcBusqueda().setVisible(false);
		//si escribe en la busque un cliente
		if(e.getComponent()==this.view.getTxtNombre()&&view.getTxtNombre().getText().trim().length()>=5&&e.getKeyCode()!=KeyEvent.VK_UP&&e.getKeyCode()!=KeyEvent.VK_DOWN&&e.getKeyCode()!=KeyEvent.VK_ENTER){

			List<Cliente> clientes=myClienteDao.buscarPorNombreTodosLosCobradores(this.view.getTxtNombre().getText(),0,10);

			view.getMntmItemBusqueda().clear();
			view.getMcBusqueda().removeAll();
			if(clientes!=null){
				for(int c=0;c<clientes.size();c++){
					JMenuItem mntmItem=new JMenuItem();
					mntmItem.setText(clientes.get(c).getId()+" | "+clientes.get(c).getNombre()+" => "+clientes.get(c).getRutaCobro().getDescripcion());
					mntmItem.setActionCommand(clientes.get(c).getId()+"");
					mntmItem.addActionListener(this);
					view.getMntmItemBusqueda().add(mntmItem);
				}
			}
			view.setMcBusqueda();
			view.getMcBusqueda().setFocusable(false);
			view.getMcBusqueda().show(view.getTxtNombre(),0,32);



		}else{
			view.getMcBusqueda().setVisible(false);
		}


	}

	private void buscarCobrador(){
		ViewListaEmpleados viewBuscarEmpleado=new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado=new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado=ctBuscarEmpleado.buscar();

		if(resultado){
			myCobrador=ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtIdCobrador().setText(myCobrador.getCodigo()+"");
			view.getTxtCobrador().setText(myCobrador.toString());
		}
	}

	private void buscarVendedor(){
		ViewListaEmpleados viewBuscarEmpleado=new ViewListaEmpleados(view);
		CtlEmpleadosListaBuscar ctBuscarEmpleado=new CtlEmpleadosListaBuscar(viewBuscarEmpleado);
		viewBuscarEmpleado.pack();
		boolean resultado=ctBuscarEmpleado.buscar();

		if(resultado){
			myVendedor=ctBuscarEmpleado.getEmpleadoSelected();
			view.getTxtIdVendedor().setText(myVendedor.getCodigo()+"");
			view.getTxtVendedor().setText(myVendedor.toString());
		}
	}

	private void buscarRuta(){
		ViewListaRutasCobro viewBuscarRuta=new ViewListaRutasCobro(view);
		CtlRutaCobroBuscar ctBuscarRuta=new CtlRutaCobroBuscar(viewBuscarRuta);
		viewBuscarRuta.pack();
		boolean resultado=ctBuscarRuta.buscar();

		if(resultado){
			myRuta=ctBuscarRuta.getRutaSelected();
			view.getTxtIdRutaCobro().setText(myRuta.getCodigo()+"");
			view.getTxtRutaCobro().setText(myRuta.getDescripcion());
		}
	}

}
