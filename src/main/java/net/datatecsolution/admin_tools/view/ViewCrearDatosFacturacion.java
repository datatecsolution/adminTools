package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlDatosFacturacion;
import net.datatecsolution.admin_tools.modelo.Caja;
import net.datatecsolution.admin_tools.view.botones.BotonActualizar;
import net.datatecsolution.admin_tools.view.botones.BotonCancelar;
import net.datatecsolution.admin_tools.view.botones.BotonGuardar;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmCajas;

import javax.swing.*;
import java.awt.*;

public class ViewCrearDatosFacturacion extends JDialog {
	
	
	private BotonCancelar btnCancelar;
	private BotonActualizar btnActualizar;
	private BotonGuardar btnGuardar;
	private JLabel lblCaja;
	private JComboBox<Caja> cbCaja;
	private JLabel lblFacturaInicial;
	private JTextField txtFacturaInicial;
	private JLabel lblFacturaFinal;
	private JTextField txtFacturaFinal;
	private JLabel lblCodigoDeFacturacion;
	private JTextField txtCodigoFacturacion;
	private JLabel lblCantidadSolicitada;
	private JTextField txtCantidadOtorgada;
	private JDateChooser fechaLimite;
	private JLabel lblFechaLimite;
	private CbxTmCajas modeloListaCajas;
	private JLabel lblCai;
	private JTextField txtCai;
	
	public ViewCrearDatosFacturacion(Window view) {
		
		super(view,"Agregar datos de facturacion", ModalityType.DOCUMENT_MODAL);
		setResizable(false);
		getContentPane().setLayout(null);
		getContentPane().setBackground(PanelPadre.color1);
		this.setFont(new Font("Verdana", Font.PLAIN, 12));
		
		btnCancelar = new BotonCancelar();
		btnCancelar.setSize(128, 78);
		btnCancelar.setLocation(222, 399);
		getContentPane().add(btnCancelar);
		
		
		
		btnGuardar = new BotonGuardar();	
		btnGuardar.setLocation(43, 400);
		getContentPane().add(btnGuardar);
		
		btnActualizar=new BotonActualizar();
		btnActualizar.setSize(136, 78);
		btnActualizar.setLocation(43, 399);
		getContentPane().add(btnActualizar);
		
		lblCaja = new JLabel("Caja");
		lblCaja.setBounds(16, 6, 61, 16);
		getContentPane().add(lblCaja);
		
		modeloListaCajas=new CbxTmCajas();//comentar para poder mostrar en forma de diseno la ventana
		
		cbCaja = new JComboBox<Caja>(modeloListaCajas);
		//cbCaja.setModel(modeloListaCajas);
		cbCaja.setBounds(16, 22, 353, 27);
		getContentPane().add(cbCaja);
		
		lblFacturaInicial = new JLabel("Factura inicial");
		lblFacturaInicial.setBounds(16, 61, 103, 16);
		getContentPane().add(lblFacturaInicial);
		
		txtFacturaInicial = new JTextField();
		txtFacturaInicial.setBounds(16, 77, 353, 26);
		getContentPane().add(txtFacturaInicial);
		txtFacturaInicial.setColumns(10);
		
		lblFacturaFinal = new JLabel("Factura final");
		lblFacturaFinal.setBounds(16, 115, 103, 16);
		getContentPane().add(lblFacturaFinal);
		
		txtFacturaFinal = new JTextField();
		txtFacturaFinal.setBounds(16, 132, 353, 26);
		getContentPane().add(txtFacturaFinal);
		txtFacturaFinal.setColumns(10);
		
		lblCodigoDeFacturacion = new JLabel("Codigo de facturacion");
		lblCodigoDeFacturacion.setBounds(16, 170, 148, 16);
		getContentPane().add(lblCodigoDeFacturacion);
		
		txtCodigoFacturacion = new JTextField();
		txtCodigoFacturacion.setBounds(16, 187, 353, 26);
		getContentPane().add(txtCodigoFacturacion);
		txtCodigoFacturacion.setColumns(10);
		
		lblCantidadSolicitada = new JLabel("Cantidad otorgada");
		lblCantidadSolicitada.setBounds(16, 225, 201, 16);
		getContentPane().add(lblCantidadSolicitada);
		
		txtCantidadOtorgada = new JTextField();
		txtCantidadOtorgada.setBounds(16, 242, 353, 26);
		getContentPane().add(txtCantidadOtorgada);
		txtCantidadOtorgada.setColumns(10);
		btnActualizar.setVisible(false);
		
		fechaLimite = new JDateChooser("dd/MM/yyyy", "##/##/####", '_');
		getContentPane().add(fechaLimite);
		fechaLimite.setBounds(16, 297, 353, 26);
		
		lblFechaLimite = new JLabel("Fecha limite ");
		lblFechaLimite.setBounds(16, 280, 103, 16);
		getContentPane().add(lblFechaLimite);
		
		lblCai = new JLabel("CAI");
		lblCai.setBounds(16, 335, 61, 16);
		getContentPane().add(lblCai);
		
		txtCai = new JTextField();
		txtCai.setBounds(16, 351, 353, 26);
		getContentPane().add(txtCai);
		txtCai.setColumns(10);
		
		this.setSize(393, 530);
		
		//centrar la ventana en la pantalla
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		
	}
	
	public void conectarCtl(CtlDatosFacturacion c){
		btnGuardar.addActionListener(c);
		btnGuardar.setActionCommand("GUARDAR");
		
		btnCancelar.addActionListener(c);
		btnCancelar.setActionCommand("CANCELAR");
		
		btnActualizar.addActionListener(c);
		btnActualizar.setActionCommand("ACTUALIZAR");	
		
	}

	/**
	 * @return the cbCaja
	 */
	public JComboBox getCbCaja() {
		return cbCaja;
	}

	/**
	 * @return the txtFacturaInicial
	 */
	public JTextField getTxtFacturaInicial() {
		return txtFacturaInicial;
	}

	/**
	 * @return the txtFacturaFinal
	 */
	public JTextField getTxtFacturaFinal() {
		return txtFacturaFinal;
	}

	/**
	 * @return the txtCodigoFacturacion
	 */
	public JTextField getTxtCodigoFacturacion() {
		return txtCodigoFacturacion;
	}

	/**
	 * @return the txtCantidadOtorgada
	 */
	public JTextField getTxtCantidadOtorgada() {
		return txtCantidadOtorgada;
	}

	/**
	 * @return the fechaLimite
	 */
	public JDateChooser getFechaLimite() {
		return fechaLimite;
	}

	/**
	 * @return the modeloListaCajas
	 */
	public CbxTmCajas getModeloListaCajas() {
		return modeloListaCajas;
	}

	/**
	 * @return the txtCai
	 */
	public JTextField getTxtCai() {
		return txtCai;
	}

	/**
	 * @return the btnCancelar
	 */
	public BotonCancelar getBtnCancelar() {
		return btnCancelar;
	}

	/**
	 * @return the btnActualizar
	 */
	public BotonActualizar getBtnActualizar() {
		return btnActualizar;
	}

	/**
	 * @return the btnGuardar
	 */
	public BotonGuardar getBtnGuardar() {
		return btnGuardar;
	}

}
