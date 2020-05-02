package net.datatecsolution.admin_tools.view.tablemodel;

import net.datatecsolution.admin_tools.modelo.CuentaFactura;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class TmFacturasCredito extends TablaModelo {
	final private String []columnNames= {
			"Fecha","Cliente", "Saldo Anterior", "Pago","Saldo Actual"
		};
	private List<CuentaFactura> cuentasFacturas=new ArrayList<CuentaFactura>();
	private BigDecimal pago=new BigDecimal(0);
	public CuentaFactura getCuenta(int row){
		return cuentasFacturas.get(row);
	}
	public void agregarCuenta(CuentaFactura f){
		cuentasFacturas.add(f);
		fireTableDataChanged();
	}
	@Override
	public String getColumnName(int columnIndex) {
	        return columnNames[columnIndex];
	        
	  }

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cuentasFacturas.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		switch (columnIndex) {
		case 0:
			return cuentasFacturas.get(rowIndex).getFecha();
		case 1:
			return cuentasFacturas.get(rowIndex).getCliente().getNombre();

		case 2:
			return cuentasFacturas.get(rowIndex).getSaldo();
		case 3:
			return cuentasFacturas.get(rowIndex).getPago();
		case 4:
			return cuentasFacturas.get(rowIndex).getNewSaldo();//cuentasFacturas.get(rowIndex).getSaldo().subtract(cuentasFacturas.get(rowIndex).getPago()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		default:
				return null;
		}
	}
	@Override
    public Class getColumnClass(int columnIndex) {
		//        return getValueAt(0, columnIndex).getClass();
        return String.class;
    }
	public void limpiarCuentas() {
		// TODO Auto-generated method stub
		cuentasFacturas.clear();
		fireTableDataChanged();
	}
	public void eliminarCuenta(int row) {
		// TODO Auto-generated method stub
		cuentasFacturas.remove(row);
		fireTableDataChanged();
	}
	public BigDecimal getPago() {
		return pago;
	}
	public void setPago(BigDecimal pago) {
		this.pago = pago;
		distrubuirPago();
	}
	private void distrubuirPago() {
		// TODO Auto-generated method stub
		//se recoren las cuentas de las facturas
		for(int x=0;x<cuentasFacturas.size();x++){
			//se estre una por una
			CuentaFactura una=cuentasFacturas.get(x);
			
			//se evalua si el saldo es igual al pago
			if(una.getSaldo().doubleValue()==this.pago.doubleValue()){
				//se ser asi
				//el pago a la factura sera el pago total
				una.setPago(new BigDecimal(pago.doubleValue()));
				//se establece en cero el pago total
				pago= new BigDecimal(0);
				//se establece el nuevo saldo en cero
				una.setNewSaldo(new BigDecimal(0));
				
				//y se sale del ciclo porque no queda saldo para realizar mas pago
				break;
			}else if(una.getSaldo().doubleValue()<this.pago.doubleValue()){
				//si el saldo de la factura es menos que el pago total
				//el pago a la factura sera el total del saldo de esta
				una.setPago(new BigDecimal(una.getSaldo().doubleValue()));
				//el nuevo saldo es cero
				una.setNewSaldo(new BigDecimal(0));
				//si el nuevo pago total sera la resta de este pago
				pago=pago.subtract(una.getSaldo());
			}else{
				//si el saldo es mayor al pago
				//el pago a la factura sera el total de pago
				una.setPago(new BigDecimal(pago.doubleValue()));
				//el nuevo saldo sera la resta del pago total menos el saldo
				una.setNewSaldo(new BigDecimal(una.getSaldo().doubleValue()).subtract(pago));
				
				//y el pago total se estable en cero
				pago= new BigDecimal(0);
				break;
			}
		}
		fireTableDataChanged();
	}
	/**
	 * @return the cuentasFacturas
	 */
	public List<CuentaFactura> getCuentasFacturas() {
		return cuentasFacturas;
	}
	/**
	 * @param cuentasFacturas the cuentasFacturas to set
	 */
	public void setCuentasFacturas(List<CuentaFactura> cuentasFacturas) {
		this.cuentasFacturas = cuentasFacturas;
	}
	public void resetPago() {
		// TODO Auto-generated method stub
		for(int x=0;x<cuentasFacturas.size();x++){
			cuentasFacturas.get(x).setPago(new BigDecimal(0));
			cuentasFacturas.get(x).setNewSaldo(new BigDecimal(0));
		}
		fireTableDataChanged();
	}
	

}
