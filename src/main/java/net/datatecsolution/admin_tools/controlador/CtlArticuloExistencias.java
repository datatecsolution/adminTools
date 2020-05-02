package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.AbstractJasperReports;
import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.Departamento;
import net.datatecsolution.admin_tools.modelo.dao.ArticuloDao;
import net.datatecsolution.admin_tools.view.ViewArticuloExistencias;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CtlArticuloExistencias implements ActionListener {
	
	
	private ViewArticuloExistencias view;
	private Articulo myArticulo;
	private Departamento myBodega;
	
	private boolean resultado=false;
	
	private ArticuloDao articuloDao;

	public CtlArticuloExistencias(ViewArticuloExistencias v) {
		// TODO Auto-generated constructor stub
		view =v;
		
		view.conectarCtl(this);
		
		articuloDao=new ArticuloDao();
		
		
		//view.setVisible(true);
	}
	public boolean actualizar(Articulo articulo,Departamento bodega){
		
		if(articulo!=null && bodega!=null){
			myArticulo=articulo;
			myBodega=bodega;
			setView();
		
			view.setVisible(true);
		
		}
		return resultado;
		
		
	}

	private void setView() {
		view.getTxtArticulo().setText(myArticulo.getArticulo());
		view.getTxtBodega().setText(myBodega.getDescripcion());
		view.getTxtExistencia().setText(myArticulo.getExistencia()+"");
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		
			case "GUARDAR":
				
				if(AbstractJasperReports.isNumberReal(view.getTxtExistencia().getText()) || AbstractJasperReports.isNumber(view.getTxtExistencia().getText()))
				{
					double newExistencia=Double.parseDouble(view.getTxtExistencia().getText());
					
					
					
					int resu=JOptionPane.showConfirmDialog(view, "Desea sumar la nueva existencia a la actual?");
					if(resu==0){
						newExistencia=newExistencia+myArticulo.getExistencia();
						
					}
					
					if(newExistencia>=0){
					
							myArticulo.setExistencia(newExistencia);
							
							//JOptionPane.showMessageDialog(view, newExistencia);
							
							boolean resul=articuloDao.actualizarExistencia(myArticulo, myBodega);
							
							if(resul){
								this.resultado=true;
								
								view.setVisible(false);
							}
					}else{
						JOptionPane.showMessageDialog(view, "No se puede establecer el nuevo inventario porque aun se queda en negativo!!!","Error ",JOptionPane.ERROR_MESSAGE);
					}
				}
				
			
					
					
			break;
			case "CANCELAR":
				view.setVisible(false);
				break;
		}
		
	}

}
