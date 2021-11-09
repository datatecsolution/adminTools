package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.modelo.DetalleFactura;
import net.datatecsolution.admin_tools.modelo.Factura;
import net.datatecsolution.admin_tools.modelo.dao.DevolucionesDao;
import net.datatecsolution.admin_tools.view.ViewFacturaDevolucion;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CtlDevoluciones implements ActionListener, MouseListener, TableModelListener, WindowListener, KeyListener {
	
	private ViewFacturaDevolucion view;
	
	
	private Factura myFactura=null;
	
	private boolean resultado=false;
	
	
	private DevolucionesDao devolucionDao=null;


	private int tipoView=1;

	public CtlDevoluciones(ViewFacturaDevolucion v) {
		// TODO Auto-generated constructor stub
		view=v;
		myFactura=new Factura();
		view.conectarContralador(this);
		devolucionDao=new DevolucionesDao();
	}
	
	private void guardar() {
		
		boolean resultado=false;
		
		//se verifica que hay item para selecciona y marcados para devolver por el usuario
		if(view.getModeloTabla().getDetalles().size()>0 && view.getModeloTabla().hayDevoluciones()==true){
			
				//se recorren los item en busca de los que se seran devueltos 
				for(int x=0;x<view.getModeloTabla().getDetalles().size();x++){
					
					//se el item esta marcado para devolicon se procede hacerlo
					if(view.getModeloTabla().getDetalles().get(x).getAccion()==true){
						//se pregunta la cantidad del item que desea devolver 
						String entrada=JOptionPane.showInputDialog(view.getModeloTabla().getDetalles().get(x).getArticulo().getArticulo()+" cantidad a devolver:");

						//conseguir la cantidad total del producto en la factura
						BigDecimal canFactura=new BigDecimal(0.0);
						canFactura=canFactura.add(view.getModeloTabla().getDetalles().get(x).getCantidad());


						//se verifica la existencia de una devolucion previa
						List<DetalleFactura> detallesDevs=devolucionDao.getDevolucionArticulo( myFactura.getIdFactura(), view.getModeloTabla().getDetalles().get(x).getArticulo().getId());
						
						if(detallesDevs!=null){

							//conseguir la cantidad que se devolvio en los diferentes item
							BigDecimal can=new BigDecimal(0.0);
							for(int c=0;c<detallesDevs.size();c++)
							{
								can=can.add(detallesDevs.get(c).getCantidad());
							}


							//se verifica que la cantidad nueva a devolver no exceda lo facturado y ya devolvido
							if(can.add(new BigDecimal(entrada)).doubleValue() <= canFactura.doubleValue()){


								//se recoge la row de la factura
								DetalleFactura unDetalle=view.getModeloTabla().getDetalles().get(x);

								//se cambia la cantidad en la row
								unDetalle.setCantidad(new BigDecimal(entrada));


								//get cantidad de la row para calcular el total
								BigDecimal cantidad=view.getModeloTabla().getDetalles().get(x).getCantidad();
								//get precio venta de la row para calcular el total
								BigDecimal precioVenta= new BigDecimal(unDetalle.getArticulo().getPrecioVenta());

								//se calcula el total del item
								BigDecimal totalItem=cantidad.multiply(precioVenta);

								//se establece el nuevo total
								unDetalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

								//se manda a guardar la devolucion
								devolucionDao.agregarDetalle(view.getModeloTabla().getDetalles().get(x), myFactura.getIdFactura());

								resultado=true;
							}else{
								JOptionPane.showMessageDialog(view, "No puede devolver la cantidad de "+entrada+" del articulo "+view.getModeloTabla().getDetalles().get(x).getArticulo().getArticulo(),"Error de validacion!!",JOptionPane.ERROR_MESSAGE);
								resultado=false;
							}
						}else if(new BigDecimal(entrada).floatValue()<=canFactura.doubleValue()){


								//se recoge la row de la factura
								DetalleFactura unDetalle=view.getModeloTabla().getDetalles().get(x);
								
								//se cambia la cantidad en la row
								unDetalle.setCantidad(new BigDecimal(entrada));


								//get cantidad de la row para calcular el total
								BigDecimal cantidad=view.getModeloTabla().getDetalles().get(x).getCantidad();
								//get precio venta de la row para calcular el total
								BigDecimal precioVenta= new BigDecimal(unDetalle.getArticulo().getPrecioVenta());

								//se calcula el total del item
								BigDecimal totalItem=cantidad.multiply(precioVenta);

								//se establece el nuevo total
								unDetalle.setTotal(totalItem.setScale(2, BigDecimal.ROUND_HALF_EVEN));

								
								//se manda a guardar la devolucion 
								devolucionDao.agregarDetalle(view.getModeloTabla().getDetalles().get(x), myFactura.getIdFactura());
								
								resultado=true;
							}else{
								JOptionPane.showMessageDialog(view, "No puede devolver "+entrada+" "+view.getModeloTabla().getDetalles().get(x).getArticulo().getArticulo(),"Error de validacion!!",JOptionPane.ERROR_MESSAGE);
								resultado=false;
							}
							

					}
				}
				
				
				
				if(resultado==true) {

					this.view.setVisible(false);
					try {

						AbstractJasperReports.createReportDevolucionVenta(ConexionStatic.getPoolConexion().getConnection(), myFactura.getIdFactura());

						AbstractJasperReports.showViewer(view);

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			
		}else{
			JOptionPane.showMessageDialog(view, "Seleccione por lo menos un articulo de la factura");
		}
		
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
		view.setVisible(false);
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
		
		switch(comando){
		case "GUARDAR":
			guardar();
			break;
		}
		
	}
public void cargarFacturaView(){
		
		this.view.getTxtIdcliente().setText(""+myFactura.getCliente().getId());
    	this.view.getTxtNombrecliente().setText(myFactura.getCliente().getNombre());
		
		
		//view.getModeloEmpleados().addEmpleado(myFactura.getVendedor());


		view.getTxtCaja().setText(ConexionStatic.getUsuarioLogin().getCajaActiva().getDescripcion());
		
		//se establece el total e impuesto en el vista
		this.view.getTxtTotal().setText(""+myFactura.getTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtImpuesto().setText(""+myFactura.getTotalImpuesto().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		this.view.getTxtSubtotal().setText(""+myFactura.getSubTotal().setScale(2, BigDecimal.ROUND_HALF_EVEN));
		view.getTxtFechafactura().setText(myFactura.getFecha());
		this.view.getModeloTabla().setDetalles(myFactura.getDetalles());
	}


	public Factura actualizarFactura(Factura f) {
		// TODO Auto-generated method stub
		this.myFactura=f;
		cargarFacturaView();
		//this.view.getBtnGuardar().setVisible(false);
		//this.view.getBtnActualizar().setVisible(true);
		this.view.getModeloTabla().agregarDetalle();
		tipoView=2;
		this.view.setVisible(true);
		
		//para controlar que es una actualizacion la que se hace
		
		
		return myFactura;
		
	}


	public boolean getAccion() {
		view.setVisible(true);
		return resultado;
	}

}
