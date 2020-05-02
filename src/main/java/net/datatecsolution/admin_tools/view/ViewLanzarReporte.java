package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.modelo.Conexion;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ViewLanzarReporte extends JDialog {
	public ViewLanzarReporte() {
		
		getContentPane().setLayout(null);
		
		JButton btnReporte = new JButton("Reporte");
		final Conexion conexion=new Conexion();
		btnReporte.setIcon(new ImageIcon(ViewLanzarReporte.class.getResource("/membrete2.png")));
		btnReporte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				Connection conn=null;
				
				try {
					 conn=conexion.getPoolConexion().getConnection();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				JasperReport jr=null;
				try {
					
					 /*JasperReport jasperReport = JasperCompileManager.compileReport("/Reportes/Factura_Saint_Paul.jrxml");
					    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,new HashMap(), new JREmptyDataSource());
					    JasperExportManager.exportReportToPdfFile(jasperPrint, "sample.pdf");*/
					 Map parametros = new HashMap();
					 parametros.put("numero_factura",new Integer(7));
					jr= (JasperReport) JRLoader.loadObjectFromFile("/Factura_Saint_Paul.jasper");
					
					JasperPrint jp=JasperFillManager.fillReport(jr, parametros,conn);
					JasperViewer jv=new JasperViewer(jp, false);
					
					jv.setVisible(true);
					
					
				} catch (JRException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					conn.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		btnReporte.setBounds(160, 51, 89, 23);
		getContentPane().add(btnReporte);
		this.setSize(367, 295);
	}
	
	public static void main(String []arg){
		ViewLanzarReporte view =new ViewLanzarReporte();
		view.setVisible(true);
	}
}
