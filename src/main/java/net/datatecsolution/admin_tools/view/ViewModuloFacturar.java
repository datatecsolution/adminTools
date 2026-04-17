package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.*;
import net.datatecsolution.admin_tools.modelo.ConexionStatic;
import net.datatecsolution.admin_tools.view.botones.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

public class ViewModuloFacturar extends JFrame {
	
	private final JDesktopPane elEscritorio;
	
	private final List<ViewFacturarFrame> ventanas=new ArrayList<ViewFacturarFrame>();
	private final BotonAgregar btnAgregar;
	private final BotonSalida btnSalidas;
	private final BotonCliente btnClientes;
	private final BotonProveedor btnProveedores;
	public BotonesApp btnCaja;
	public BotonesApp btnUsuario;
	public BotonesApp btnFecha;

	private final BotonDescuento btnDescuento;

	private final BotonPrecio btnPrecio;

	private final BotonCantidad btnCantidad;

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
		btnDescuento.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().aplicarDescuento();
		});
		barra.add(btnDescuento);

		btnPrecio=new BotonPrecio();
		btnPrecio.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().modificarPrecio();
		});
		barra.add(btnPrecio);

		btnCantidad=new BotonCantidad();
		btnCantidad.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().modificarCantidad();
		});
		barra.add(btnCantidad);

		btnClientes = new BotonCliente();
		btnClientes.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().abrirCobros();
		});
		barra.add(btnClientes);

		btnProveedores = new BotonProveedor();
		btnProveedores.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().abrirPagoProveedor();
		});
		barra.add(btnProveedores);

		btnSalidas = new BotonSalida();
		btnSalidas.addActionListener(e -> {
			ViewFacturarFrame activo=(ViewFacturarFrame)elEscritorio.getSelectedFrame();
			if(activo!=null) activo.getCtl().abrirSalidaCaja();
		});
		barra.add(btnSalidas);
		
		
		
		int alturaBarra = btnAgregar.getPreferredSize().height;

		btnCaja=new BotonesApp();
		btnCaja.setText("Caja: "+ConexionStatic.getUsuarioLogin().getCajaActiva().getDescripcion());
		btnCaja.setPreferredSize(new Dimension(btnCaja.getPreferredSize().width, alturaBarra));
		btnCaja.setMaximumSize(new Dimension(btnCaja.getPreferredSize().width, alturaBarra));
		barra.add(btnCaja);

		btnUsuario=new BotonesApp();
		btnUsuario.setText("User: "+ConexionStatic.getUsuarioLogin().getUser());
		btnUsuario.setPreferredSize(new Dimension(btnUsuario.getPreferredSize().width, alturaBarra));
		btnUsuario.setMaximumSize(new Dimension(btnUsuario.getPreferredSize().width, alturaBarra));
		barra.add(btnUsuario);

		btnFecha=new BotonesApp(){
			@Override
			public void setText(String text) {
				super.setText(text);
				Dimension d = getPreferredSize();
				setMaximumSize(new Dimension(d.width, alturaBarra));
			}
		};
		btnFecha.setText("Fecha: ");
		barra.add(btnFecha);
		
		
		// establece componente de escucha para el elemento de menú nuevoMarco
		btnAgregar.addActionListener(
			new ActionListener() // clase interna anónima
			{
				 // muestra la nueva ventana interna
				 public void actionPerformed( ActionEvent evento )
				 {
				 	//se verifica para limpiar el array si no hay ventana visible
					if(ventanas.size()==1 && ventanas.get(0).isClosed()==true) {
						ventanas.clear();
					}

					//se agrega la ventana de facturacion solo no hay ninguna ventada visible
					 if(ventanas.size()<1) {
						 // crea el marco interno
						 ViewFacturarFrame marco = new ViewFacturarFrame("Factura1", true, true, true, true);
						 CtlFacturarFrame ctlMarco = new CtlFacturarFrame(marco, ventanas);

						 elEscritorio.add(marco); // adjunta marco interno
						 btnFecha.setText("Fecha: " + marco.getTxtFechafactura().getText());
						 btnFecha.revalidate();

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
						 marco.setVisible(true); // muestra marco interno
						 SwingUtilities.invokeLater(() -> marco.getTxtBuscar().requestFocusInWindow());
					 }

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

