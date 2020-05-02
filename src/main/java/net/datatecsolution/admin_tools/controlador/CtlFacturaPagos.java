package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.ClienteDao;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;
import net.datatecsolution.admin_tools.modelo.dao.ReciboPagoDao;
import net.datatecsolution.admin_tools.view.ViewCxCPagos;
import net.datatecsolution.admin_tools.view.ViewListaClientes;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CtlFacturaPagos implements ActionListener, MouseListener, TableModelListener, WindowListener, KeyListener {
	private ViewCxCPagos view=null;
	
	private Cliente myCliente=null;
	private ClienteDao clienteDao=null;
	private FacturaDao myFacturaDao=null;
	
	private ReciboPago myRecibo=null;
	private ReciboPagoDao myReciboDao=null;
	

	public CtlFacturaPagos(ViewCxCPagos v) {
		// TODO Auto-generated constructor stub
		view=v;
		view.conectarContralador(this);
		clienteDao=new ClienteDao();
		myFacturaDao=new FacturaDao();
		myReciboDao=new ReciboPagoDao();
		myRecibo=new ReciboPago();
		view.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		this.view.setVisible(false);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
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
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		int colum=e.getColumn();
		int row=e.getFirstRow();
		//JOptionPane.showMessageDialog(view, myArticulo);
		//JOptionPane.showMessageDialog(view, "paso de celdas");
		switch(e.getType()){
		
		
		
			case TableModelEvent.UPDATE:
				
				//Se recoge el id de la fila marcada
		        int identificador=0; 
				
				
				//se agrego un descuento a la tabla
				if(colum==4){
					calcularTotales();
					//JOptionPane.showMessageDialog(view, "Modifico el Descuento "+this.view.getModeloTabla().getDetalle(row).getDescuentoItem().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				}
				
				//view.getTxtBuscar().requestFocusInWindow();
			break;
		}
		
	
	}

	private void calcularTotales() {
		
		this.myRecibo.resetTotales();
		for(int x=0; x<this.view.getModeloTabla().getDetalles().size();x++){
			
			Factura detalle=this.view.getModeloTabla().getDetalle(x);
			
			if(detalle.getDeseaPagar()==true){
			
				//this.total.add(detalle.getTotal());
				myRecibo.setTotal(detalle.getTotal());
				
			
				//JOptionPane.showMessageDialog(view, "fecha:"+total);
					
			}		
					
				
				
			
		}//fin del for
		
		this.view.getTxtTotal().setText(""+myRecibo.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		//JOptionPane.showMessageDialog(view, "paso de celdas");
		switch(comando){
		case "BUSCARCLIENTE":
			myCliente=null;
			myCliente=clienteDao.buscarPorId(Integer.parseInt(this.view.getTxtIdcliente().getText()));
			
			if(myCliente!=null){
				this.view.getTxtNombrecliente().setText(myCliente.getNombre());
				view.getTxtLimiteCredito().setText("L. "+myCliente.getLimiteCredito());
				view.getTxtSaldo().setText("L. "+myCliente.getSaldoCuenta());
				cargarFacturasClientes(myFacturaDao.sinPagarCliente(myCliente));
			}else{
				this.view.getTxtIdcliente().setText("");
				this.view.getTxtNombrecliente().setText("");
				JOptionPane.showMessageDialog(view, "Cliente no encontrado");
			}
			
			break;
			
		case "BUSCARCLIENTES":
			this.buscarCliente();
			break;
			
		case "SELECTALL":
				view.getModeloTabla().cambiarEstado(view.getChckbxTodos().isSelected());
				calcularTotales();
				//JOptionPane.showMessageDialog(view, "Se cambio el cambio del todos" +view.getChckbxTodos().isSelected());
			break;
			
		case "COBRAR":
			 cobrar();
			break;
		
		}
		
		
	}
	
	private void cobrar() {
		//se verifica que hay facturas que cobrar
		if(view.getModeloTabla().getDetalles().size()>0 && view.getModeloTabla().hayPagos()==true){
			setRecibo();
			//se manda aguardar el recibo con los pagos realizados
			boolean resul=this.myReciboDao.registrar(myRecibo);
			
			if(resul){
				
				myRecibo.setNoRecibo(myReciboDao.idUltimoRecibo);
				//se cambia el estado de las factura que fueron cobradas
				for(int x=0;x<view.getModeloTabla().getDetalles().size();x++){
					
					//se cambia el estado de las facturas pagas
					if(view.getModeloTabla().getDetalles().get(x).getDeseaPagar()==true){
						boolean resu=this.myFacturaDao.cambiarEstadoPago(view.getModeloTabla().getDetalles().get(x).getIdFactura());
					}
				}
				JOptionPane.showMessageDialog(view, "El recibo se guardo correctamente.");
				this.view.setVisible(false);
				
				
				try {
					/*this.view.setVisible(false);
					this.view.dispose();*/
					//AbstractJasperReports.createReportFactura( conexion.getPoolConexion().getConnection(), "Factura_Saint_Paul.jasper",myFactura.getIdFactura() );
					AbstractJasperReports.createReport(ConexionStatic.getPoolConexion().getConnection(), 5, myRecibo.getNoRecibo());
					//AbstractJasperReports.showViewer(view);
					AbstractJasperReports.showViewer(view);
					
					//myFactura.
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else{//
				JOptionPane.showMessageDialog(view, "El recibo no se guardo correctamente.");
			}//fin del if que verefica la acccion de guardar el recibo
		}else{
			JOptionPane.showMessageDialog(view, "No tiene facturas que pagar el cliente.");
		}
		
	}

	private void setRecibo() {
		// TODO Auto-generated method stub
		myRecibo.setCliente(myCliente);
		
		//se estable el concepto de pago del recibo
		String concepto="Pago de factura(s) no.";
		
		for(int x=0;x<view.getModeloTabla().getDetalles().size();x++){
			
			//se verifica la facturas que se van a pagar y se arma el concepto
			if(view.getModeloTabla().getDetalles().get(x).getDeseaPagar()==true){
				concepto=concepto+view.getModeloTabla().getDetalles().get(x).getIdFactura()+",";
				myRecibo.getFacturas().add(view.getModeloTabla().getDetalles().get(x));
			}
		}
		myRecibo.setConcepto(concepto);
		//se establece la cantidad en letras
		myRecibo.setTotalLetras(NumberToLetterConverter.convertNumberToLetter(myRecibo.getTotal().setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue()));
	}

	private void buscarCliente(){
		//se crea la vista para buscar los cliente
		ViewListaClientes viewListaCliente=new ViewListaClientes (this.view);
		
		CtlClienteBuscar ctlBuscarCliente=new CtlClienteBuscar(viewListaCliente);
		
		boolean resul=ctlBuscarCliente.buscarCliente(view);
		//se comprueba si le regreso un articulo valido
		if(resul){
			myCliente=ctlBuscarCliente.getCliente();
			
			this.view.getTxtIdcliente().setText(""+myCliente.getId());
            this.view.getTxtNombrecliente().setText(myCliente.getNombre());
			view.getTxtLimiteCredito().setText("L. "+myCliente.getLimiteCredito());
			view.getTxtSaldo().setText("L. "+myCliente.getSaldoCuenta());
			cargarFacturasClientes(myFacturaDao.sinPagarCliente(myCliente));
		
		}else{
			JOptionPane.showMessageDialog(view, "No se encontro el cliente");
			this.view.getTxtIdcliente().setText("1");
            this.view.getTxtNombrecliente().setText("Cliente Normal");
		}
		viewListaCliente.dispose();
		ctlBuscarCliente=null;
	}

	private void cargarFacturasClientes(List<Factura> facturas) {
		// TODO Auto-generated method stub
		this.view.getModeloTabla().limpiarFacturas();
		
		if(facturas!=null){
			for(int c=0;c<facturas.size();c++){
				this.view.getModeloTabla().agregarFactura(facturas.get(c));
				
			}
		}
	}

}
