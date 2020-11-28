package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.*;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.view.botones.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

public class ViewModuloFacturar extends JFrame {
	
	private JDesktopPane elEscritorio;
	
	private List<ViewFacturarFrame> ventanas=new ArrayList<ViewFacturarFrame>();
	private BotonAgregar btnAgregar;
	private BotonSalida btnSalidas;
	private BotonCliente btnClientes;
	private BotonProveedor btnProveedores;
	public BotonesApp btnCaja;
	public BotonesApp btnUsuario;

	private BotonDescuento btnDescuento;

	private BotonPrecio btnPrecio;

	private BotonCantidad btnCantidad;

	public ViewModuloFacturar() {
		super( "Facturacion" );
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(ViewFacturar.class.getResource("/drawable/logo-admin-tool1.png")));
		
		JMenuBar barra = new JMenuBar();
		setJMenuBar( barra ); // establece la barra de menús para esta aplicación
		

		elEscritorio = new JDesktopPane(); // crea el panel de escritorio
		getContentPane().add( elEscritorio ); // agrega el panel de escritorio al marco
		
		btnAgregar = new BotonAgregar();
		barra.add(btnAgregar);
		
		btnDescuento =new BotonDescuento();
		btnDescuento.addActionListener(new ActionListener() {
			

			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				
				ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
				if(activo!=null){
					KeyEvent key = new KeyEvent(btnDescuento, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F7);
					activo.getCtl().keyPressed(key);
				}
			}
		});
		barra.add(btnDescuento);
		
		btnPrecio=new BotonPrecio();
		btnPrecio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
				if(activo!=null){
					KeyEvent key = new KeyEvent(btnPrecio, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F8);
					activo.getCtl().keyPressed(key);
				}
			}
		});
		barra.add(btnPrecio);
		
		btnCantidad=new BotonCantidad();
		btnCantidad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
				if(activo!=null){
					KeyEvent key = new KeyEvent(btnCantidad, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F9);
					activo.getCtl().keyPressed(key);
				}
			}
		});
		barra.add(btnCantidad);
		
		btnClientes = new BotonCliente();
		btnClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ViewCuentasFacturas viewCuentasFacturas=new ViewCuentasFacturas(null);
				CtlCuentasFacturas ctlCuentasFacturas=new CtlCuentasFacturas(viewCuentasFacturas);


				viewCuentasFacturas.dispose();
				ctlCuentasFacturas=null;
				/*
				ViewCobro viewCobro=new ViewCobro(null);
				CtlCobro ctlCobro=new CtlCobro(viewCobro);
				
				viewCobro.dispose();
				viewCobro=null;
				ctlCobro=null;

				 */
			}
		});
		barra.add(btnClientes);
		
		btnProveedores = new BotonProveedor();
		btnProveedores.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				ViewPagoProveedor vPagoProveedores=new ViewPagoProveedor(null);
				vPagoProveedores.getCbFormaPago().setEnabled(false);
				CtlPagoProveedor cPagoProveedores=new CtlPagoProveedor(vPagoProveedores);
				
				vPagoProveedores.dispose();
				cPagoProveedores=null;
			}
		});
		barra.add(btnProveedores);
		
		btnSalidas = new BotonSalida();
		btnSalidas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewSalidaCaja viewSalida = new ViewSalidaCaja(null);
				CtlSalidaCaja ctlSalida = new CtlSalidaCaja(viewSalida);

				viewSalida.dispose();
				viewSalida = null;
				ctlSalida = null;
			}
		});
		barra.add(btnSalidas);
		
		
		
		btnCaja=new BotonesApp();
		btnCaja.setText("caja: "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDescripcion());
		barra.add(btnCaja);

		btnUsuario=new BotonesApp();
		btnUsuario.setText("User: "+ConexionStatic.getUsuarioLogin().getUser());
		barra.add(btnUsuario);
		
		
		// establece componente de escucha para el elemento de menú nuevoMarco
		btnAgregar.addActionListener(

		new ActionListener() // clase interna anónima
		{
		 // muestra la nueva ventana interna
		 public void actionPerformed( ActionEvent evento )
		 {
			 // crea el marco interno
			 ViewFacturarFrame marco = new ViewFacturarFrame(
					 "Factura1", true, true, true, true );
			 CtlFacturarFrame ctlMarco=new CtlFacturarFrame(marco,ventanas);
			 
			 elEscritorio.add( marco ); // adjunta marco interno
			 
			 try {
				marco.setSelected(true);
				 //diz que a janela interna é maximizável   
				 marco.setMaximizable(true);   
		            //set o tamanho máximo dela, que depende da janela pai   
				 marco.setMaximum(true); 
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
	           
			 ventanas.add(marco);
			 ctlMarco.actualizarVentanas();
			
	
		
		 
		 marco.setVisible( true ); // muestra marco interno
		 } // fin del método actionPerformed
		 } // fin de la clase interna anónima
		 ); // fin de la llamada a addActionListener
		
		
		setSize(this.getToolkit().getScreenSize());
		
		//this.setPreferredSize(new Dimension(1024, 600));
		//this.setResizable(false);
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	public void conectarContralador(CtlModuloFacturar c){
		btnAgregar.addActionListener(c);
		btnAgregar.setActionCommand("NUEVO");
		
	}
}

