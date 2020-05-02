package net.datatecsolution.admin_tools.view;

import net.datatecsolution.admin_tools.controlador.CtlArticuloExistencias;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewArticuloExistencias extends JDialog {
	private JButton btnGuardar;
	private JTextField txtArticulo;
	private JTextField txtBodega;
	private JTextField txtExistencia;
	private BotonCancelar btnCancelar;

	public ViewArticuloExistencias(Window view) {
		
		super(view,"Existencias articulo", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		// TODO Auto-generated constructor stub
		getContentPane().setBackground(PanelPadre.color1);
		this.setPreferredSize(new Dimension(212, 264));
		this.setSize(358, 307);
		// TODO Auto-generated constructor stub
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		getContentPane().setLayout(null);
		
		JLabel lblDe = new JLabel("Articulo");
		lblDe.setBounds(26, 8, 92, 16);
		getContentPane().add(lblDe);
		
		JLabel lblHasta = new JLabel("Bodega");
		lblHasta.setBounds(26, 67, 65, 16);
		getContentPane().add(lblHasta);
		
		btnGuardar = new BotonGuardar();
		btnGuardar.setBounds(32, 193, 130, 65);
		getContentPane().add(btnGuardar);
		
		JLabel lblPorcentajeSv = new JLabel("Existencias");
		lblPorcentajeSv.setBounds(26, 126, 144, 16);
		getContentPane().add(lblPorcentajeSv);
		
		txtArticulo = new JTextField();
		txtArticulo.setEditable(false);
		txtArticulo.setBounds(26, 25, 314, 30);
		getContentPane().add(txtArticulo);
		txtArticulo.setColumns(10);
		
		txtBodega = new JTextField();
		txtBodega.setEditable(false);
		txtBodega.setBounds(26, 84, 314, 30);
		getContentPane().add(txtBodega);
		txtBodega.setColumns(10);
		
		txtExistencia = new JTextField();
		txtExistencia.setBounds(26, 141, 314, 30);
		getContentPane().add(txtExistencia);
		txtExistencia.setColumns(10);
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setBounds(194, 193, 130, 65);
		getContentPane().add(btnCancelar);
	
	}
	
	public void conectarCtl(CtlArticuloExistencias c){
		btnGuardar.addActionListener( c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnCancelar.addActionListener( c);
		btnCancelar.setActionCommand("CANCELAR");
	}

	/**
	 * @return the txtArticulo
	 */
	public JTextField getTxtArticulo() {
		return txtArticulo;
	}

	/**
	 * @return the txtBodega
	 */
	public JTextField getTxtBodega() {
		return txtBodega;
	}

	/**
	 * @return the txtExistencia
	 */
	public JTextField getTxtExistencia() {
		return txtExistencia;
	}
	
}
