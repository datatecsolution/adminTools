package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.CajaDao;
import net.datatecsolution.admin_tools.modelo.dao.DetalleFacturaDao;
import net.datatecsolution.admin_tools.view.ViewFiltroRepVenc;
import net.datatecsolution.admin_tools.view.ViewListaCategorias;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CtlFiltroRepVenc implements ActionListener, KeyListener  {
	private ViewFiltroRepVenc view;
	private Categoria categoriaReporte=null;
	private Departamento bodegaReporte;
	public CtlFiltroRepVenc(ViewFiltroRepVenc v){
		
		view =v;
		view.conectarCtl(this);
		bodegaReporte=new Departamento();
		bodegaReporte.setId(1);//si implementamos la seleccion de la bodega se debe eleminar este
		
		view.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GENERAR":
				if(categoriaReporte==null){
					JOptionPane.showMessageDialog(this.view,"No seleciono una Categoria");
					break;
				}
				if(categoriaReporte.getDescripcion()!=null || categoriaReporte.getId()!=0) {

					try {

						AbstractJasperReports.createReportArticulosXvencer(ConexionStatic.getPoolConexion().getConnection(), categoriaReporte, bodegaReporte.getId(), (Integer) view.getSpDias().getValue());

						//AbstractJasperReports.imprimierFactura();
						AbstractJasperReports.showViewer(view);
					} catch (SQLException eee) {
						// TODO Auto-generated catch block
						eee.printStackTrace();
					}
				}else {
					JOptionPane.showMessageDialog(this.view,"No seleciono una Categoria");
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
			ViewListaCategorias viewListaM=new ViewListaCategorias(view);
			CtlCategoriaBuscar ctl=new CtlCategoriaBuscar(viewListaM);
			viewListaM.conectarControladorBusqueda(ctl);
			//se crea una marca y se llena con la busqueda que selecciona el usuario
			Categoria myCategoria=ctl.buscarMarca();

			//se compara si el usuario selecciono un marca
			if(myCategoria.getDescripcion()!=null && myCategoria.getId()!=0){
				//se pasa la marca buscada y selecciona al nuevo articulo
				//myArticulo.setCategoria(myCategoria);

				//se muestra el nombre de la marca en la caja de texto
				view.getTxtCategoria().setText(myCategoria.getDescripcion());
				categoriaReporte=myCategoria;

			}else{
				JOptionPane.showMessageDialog(this.view,"No seleciono una Categoria");
				//myArticulo.getCategoria().setId(-1);
			}
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	

}
