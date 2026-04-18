package net.datatecsolution.admin_tools.view;

import com.toedter.calendar.JDateChooser;
import net.datatecsolution.admin_tools.controlador.CtlFiltroRepVentasArtVendedor;
import net.datatecsolution.admin_tools.view.rendes.PanelPadre;

import javax.swing.*;
import java.awt.*;

public class ViewFiltroRepVentasArtVendedor extends JDialog {

	private final JDateChooser dCBuscar1;
	private final JDateChooser dCBuscar2;
	private final JTextField txtArticulo;
	private final JTextField txtVendedor;
	private final JButton btnBuscar;

	public ViewFiltroRepVentasArtVendedor(Window view) {

		super(view, "Reporte ventas articulo por vendedor", ModalityType.DOCUMENT_MODAL);
		setResizable(false);

		getContentPane().setBackground(PanelPadre.color1);

		this.setSize(512, 320);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		getContentPane().setLayout(null);

		JLabel lblRangoFecha = new JLabel("Selecione rango fecha");
		lblRangoFecha.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblRangoFecha.setBounds(6, 6, 194, 16);
		getContentPane().add(lblRangoFecha);

		JLabel lblDe = new JLabel("De:");
		lblDe.setBounds(6, 34, 26, 16);
		getContentPane().add(lblDe);

		dCBuscar1 = new JDateChooser();
		dCBuscar1.setDateFormatString("dd-MM-yyyy");
		dCBuscar1.setPreferredSize(new Dimension(160, 27));
		dCBuscar1.setBounds(33, 34, 194, 27);
		getContentPane().add(dCBuscar1);

		JLabel lblHasta = new JLabel("Hasta:");
		lblHasta.setBounds(253, 34, 48, 16);
		getContentPane().add(lblHasta);

		dCBuscar2 = new JDateChooser();
		dCBuscar2.setDateFormatString("dd-MM-yyyy");
		dCBuscar2.setPreferredSize(new Dimension(160, 27));
		dCBuscar2.setBounds(314, 34, 175, 27);
		getContentPane().add(dCBuscar2);

		JLabel lblArticulo = new JLabel("Articulo (F1)");
		lblArticulo.setBounds(6, 78, 103, 16);
		getContentPane().add(lblArticulo);

		txtArticulo = new JTextField();
		txtArticulo.setEditable(false);
		txtArticulo.setBounds(33, 96, 456, 26);
		txtArticulo.setColumns(10);
		getContentPane().add(txtArticulo);

		JLabel lblVendedor = new JLabel("Vendedor (F2)");
		lblVendedor.setBounds(6, 134, 120, 16);
		getContentPane().add(lblVendedor);

		txtVendedor = new JTextField();
		txtVendedor.setEditable(false);
		txtVendedor.setBounds(33, 152, 456, 26);
		txtVendedor.setColumns(10);
		getContentPane().add(txtVendedor);

		btnBuscar = new JButton("Ver Reporte");
		btnBuscar.setBounds(177, 195, 168, 49);
		getContentPane().add(btnBuscar);
	}

	public JDateChooser getBuscar1() {
		return this.dCBuscar1;
	}

	public JDateChooser getBuscar2() {
		return this.dCBuscar2;
	}

	public JTextField getTxtArticulo() {
		return txtArticulo;
	}

	public JTextField getTxtVendedor() {
		return txtVendedor;
	}

	public void conectarCtl(CtlFiltroRepVentasArtVendedor c) {
		btnBuscar.addActionListener(c);
		btnBuscar.setActionCommand("GENERAR");

		txtArticulo.addKeyListener(c);
		txtVendedor.addKeyListener(c);
		btnBuscar.addKeyListener(c);
		dCBuscar1.addKeyListener(c);
		dCBuscar2.addKeyListener(c);
	}
}
