package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.view.ViewCuentaEfectivo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;

public class CtlContarEfectivo implements ActionListener, KeyListener{
	
	
	private ViewCuentaEfectivo view;
	
	private BigDecimal total=new BigDecimal(0.0);
	
	private BigDecimal uno=new BigDecimal(0.0);
	private BigDecimal dos=new BigDecimal(0.0);
	private BigDecimal cinco=new BigDecimal(0.0);
	private BigDecimal diez=new BigDecimal(0.0);
	private BigDecimal veinte=new BigDecimal(0.0);
	private BigDecimal cincuenta=new BigDecimal(0.0);
	private BigDecimal cien=new BigDecimal(0.0);
	private BigDecimal quinientos=new BigDecimal(0.0);
	private boolean resultado=false;

	public CtlContarEfectivo(ViewCuentaEfectivo v) {
		// TODO Auto-generated constructor stub
		view=v;
		view.conectarControlador(this);
		view.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String comando=e.getActionCommand();
		
		switch(comando){
		case "GUARDAR":
			setTotal();
			break;
		case "MOSTRAR":
			/*CierreCajaDao cierreDao=new CierreCajaDao(conexion);
			CierreCaja unCierre=cierreDao.getCierre(1);
			
			JOptionPane.showMessageDialog(view, "Las ventas del usuario "+conexion.getUsuarioLogin().getUser()+" son: L. "+unCierre.getEfectivo());*/
			break;
		case "SALIR":
			resultado=false;
			view.setVisible(false);
			break;
		}
	}
	public boolean getEstado(){
		return resultado;
	}

	private void setTotal() {
		// TODO Auto-generated method stub
		uno=new BigDecimal(view.getTxtUno().getText());
		total=total.add(uno);
		
		dos=new BigDecimal(view.getTxtDos().getText());
		total=total.add(dos.multiply(new BigDecimal(2)));
		
		cinco=new BigDecimal(view.getTxtCinco().getText());
		total=total.add(cinco.multiply(new BigDecimal(5)));
		
		diez=new BigDecimal(view.getTxtDiez().getText());
		total=total.add(diez.multiply(new BigDecimal(10)));
		
		veinte=new BigDecimal(view.getTxtVeinte().getText());
		total=total.add(veinte.multiply(new BigDecimal(20)));
		
		cincuenta=new BigDecimal(view.getTxtCincuenta().getText());
		total=total.add(cincuenta.multiply(new BigDecimal(50)));
		
		cien=new BigDecimal(view.getTxtCien().getText());
		total=total.add(cien.multiply(new BigDecimal(100)));
		
		quinientos=new BigDecimal(view.getTxtQuiniento().getText());
		total=total.add(quinientos.multiply(new BigDecimal(500)));
		
		this.resultado=true;
		
		view.setVisible(false);
		
		
	}
	
	public BigDecimal getTotal(){
		return total;
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
					this.setTotal();
					break;
				case  KeyEvent.VK_ESCAPE:
					this.resultado=false;
					view.setVisible(false);
				break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
