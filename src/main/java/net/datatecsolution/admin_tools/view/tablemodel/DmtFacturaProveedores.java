package net.datatecsolution.admin_tools.view.tablemodel;


import net.datatecsolution.admin_tools.modelo.Articulo;
import net.datatecsolution.admin_tools.modelo.CodBarra;
import net.datatecsolution.admin_tools.modelo.DetalleFacturaProveedor;
import net.datatecsolution.admin_tools.modelo.PrecioArticulo;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.toedter.calendar.JDateChooser;
import org.apache.poi.ss.usermodel.DataFormat;


public class DmtFacturaProveedores extends AbstractTableModel {

	final private String []columnNames= {
			"Id Articulo", "Nombre", "Cantidad", "Precio Unidad","SubTotal","Impuesto", "Total","P/venta","P/venta 2","P/venta 3","P/costo","Fecha Venc."
	};
	private List<DetalleFacturaProveedor> detallesFactura=new ArrayList<DetalleFacturaProveedor>();
	private double totalCompra=0;

	public DmtFacturaProveedores(){
		//datosVacios();

	}

	public void agregarDetalle(){

		for(int x=0;x<detallesFactura.size();x++){

			if(detallesFactura.get(x).getArticulo().getId()<0){
				detallesFactura.remove(x);

			}
		}
		DetalleFacturaProveedor uno =new DetalleFacturaProveedor();
		detallesFactura.add(uno);
		fireTableDataChanged();
	}
	public void setTotalCompra(double t){
		totalCompra+=t;
	}
	public double getTotalCompra(){
		return totalCompra;
	}
	public void agregarDetalle(DetalleFacturaProveedor detalle) {
		detallesFactura.add(detalle);
		fireTableDataChanged();
	}

	public void setArticulo(Articulo a, int row){
		detallesFactura.get(row).setListArticulos(a);
	}


	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];

	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return detallesFactura.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

		if(detallesFactura.get(rowIndex).getArticulo().getId()==-1){
			return null;
		}
		else{
			switch (columnIndex) {
				case 0:

					return detallesFactura.get(rowIndex).getArticulo().getId();

				case 1:
					return detallesFactura.get(rowIndex).getArticulo().getArticulo();
				case 2:

					if(detallesFactura.get(rowIndex).getCantidad().doubleValue()!=0)
						return detallesFactura.get(rowIndex).getCantidad();
					else
						return null;

				case 3:

					if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
						return detallesFactura.get(rowIndex).getPrecioCompra();


					}
					else
						return null;
				case 4:
					if(detallesFactura.get(rowIndex).getCantidad().doubleValue()!=0)
						return detallesFactura.get(rowIndex).getSubTotal();
					else
						return null;

				case 5:

					if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
						return detallesFactura.get(rowIndex).getImpuesto();
					}
					else
						return null;

				case 6:

					if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
						return detallesFactura.get(rowIndex).getTotal();
					}
					else
						return null;
				case 7:
					if(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue()!=0){
						//descomentar para calcular un porcentaje de utilidad en los precios
						//BigDecimal precioVenta=new BigDecimal(detallesFactura.get(rowIndex).getPrecioCompra().doubleValue());

						//		BigDecimal newPrecioVenta=precioVenta.multiply(new BigDecimal(1.4));

						//detallesFactura.get(rowIndex).getArticulo().setPrecioVenta(newPrecioVenta.setScale(0, BigDecimal.ROUND_HALF_EVEN).doubleValue());
						//return detallesFactura.get(rowIndex).getArticulo().getPrecioVenta();
						return detallesFactura.get(rowIndex).getArticulo().getPrecioVenta();

					}else
						return null;

				case 8:
					PrecioArticulo precioVenta2=null;

					for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
						if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==2){
							precioVenta2=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
						}
					}
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioVenta2!=null){


						return precioVenta2.getPrecio();

					}else
						return null;
				case 9:
					PrecioArticulo precioVenta3=null;

					for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
						if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==3){
							precioVenta3=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
						}
					}
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioVenta3!=null){
						return precioVenta3.getPrecio();
					}else
						return null;
				case 10:
					PrecioArticulo precioCosto=null;

					for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
						if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==4){
							precioCosto=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
						}
					}
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && precioCosto!=null){


						return precioCosto.getPrecio();

					}else
						return null;

				case 11:

					// Define el formato de la cadena de fecha
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

					try {
						//fecha por defecto
						LocalDate fechaDef = LocalDate.parse("01/01/2000", formatter);

						// Convierte la cadena de fecha a LocalDate
						LocalDate fecha = LocalDate.parse(detallesFactura.get(rowIndex).getDateVencimiento(), formatter);

						if(fecha.isEqual(fechaDef)){
							return "";
						}else{
							return detallesFactura.get(rowIndex).getDateVencimiento();
						}
					} catch (DateTimeParseException e) {
						// Maneja la excepción si la cadena de fecha no tiene el formato correcto
						System.out.println("Error: Formato de fecha incorrecto.");
						e.printStackTrace();
					}




				default:
					return null;
			}
		}
	}


	public void setDetalles(List<DetalleFacturaProveedor> d){
		detallesFactura.clear();
		if(d!=null){
			for(int x=0;x<d.size();x++){
				detallesFactura.add(d.get(x));
			}
		}else
			agregarDetalle();
		//detallesFactura=d;
		fireTableDataChanged();
	}

	public DetalleFacturaProveedor getDetalle(int row){
		return detallesFactura.get(row);
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		DetalleFacturaProveedor detalle = detallesFactura.get(rowIndex);
		//JOptionPane.showMessageDialog(null, value);

		String v = null;

		if(columnIndex==11) {
			Date vv = (Date) value;
			v = new SimpleDateFormat("dd/MM/yyyy").format(vv);
		}
		else
			v=(String) value;


		switch(columnIndex){
			case 0:
				/*
				try{
					int id=Integer.parseInt(v);
					detallesFactura.get(rowIndex).getArticulo().setId(id);
					
					//this.fireTableDataChanged();
					//fireTableCellUpdated(row,col);
					//fireTableDataChanged();
				}catch(NumberFormatException e){
					CodBarra cod=new CodBarra();
					cod.setCodigoBarra(v);
					detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(cod);
					detallesFactura.get(rowIndex).getArticulo().setId(-2);
					//detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(e)
				}*/


				CodBarra cod=new CodBarra();
				cod.setCodigoBarra(v);
				detallesFactura.get(rowIndex).getArticulo().getCodBarra().add(cod);
				detallesFactura.get(rowIndex).getArticulo().setId(-2);
				this.fireTableCellUpdated(rowIndex, columnIndex);
				break;
			case 2:

				detallesFactura.get(rowIndex).setCantidad(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);
				break;
			case 3:
				detallesFactura.get(rowIndex).setPrecioCompra(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);

				break;

			case 4:
				detallesFactura.get(rowIndex).setSubTotal(new BigDecimal(v));
				fireTableCellUpdated(rowIndex, columnIndex);

				break;
			case 7:
				detallesFactura.get(rowIndex).getArticulo().setPrecioVenta(new Double(v));
				fireTableCellUpdated(rowIndex, columnIndex);
				break;

			case 8:
				PrecioArticulo precioVenta2=null;

				for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==2){
						precioVenta2=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
					}
				}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){

					precioVenta2.setPrecio(new BigDecimal(v));
					fireTableCellUpdated(rowIndex, columnIndex);

				}else{
					PrecioArticulo precioVent2=new PrecioArticulo();
					precioVent2.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
					precioVent2.setCodigoPrecio(2);
					precioVent2.setPrecio(new BigDecimal(v));
					detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioVent2);
				}

				break;
			case 9:
				PrecioArticulo precioVenta3=null;

				for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==3){
						precioVenta3=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
					}
				}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){

					precioVenta3.setPrecio(new BigDecimal(v));
					fireTableCellUpdated(rowIndex, columnIndex);

				}else{
					PrecioArticulo precioVent3=new PrecioArticulo();
					precioVent3.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
					precioVent3.setCodigoPrecio(3);
					precioVent3.setPrecio(new BigDecimal(v));
					detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioVent3);
				}

				break;
			case 10:
				PrecioArticulo precioCosto=null;

				for(int aa=0;aa<detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size();aa++){
					if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa).getCodigoPrecio()==4){
						precioCosto=detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().get(aa);
					}
				}
				if(detallesFactura.get(rowIndex).getArticulo().getPreciosVenta()!=null && detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().size()>=3){

					precioCosto.setPrecio(new BigDecimal(v));
					fireTableCellUpdated(rowIndex, columnIndex);

				}else{
					PrecioArticulo precioCosto2=new PrecioArticulo();
					precioCosto2.setCodigoArticulo(detallesFactura.get(rowIndex).getArticulo().getId());
					precioCosto2.setCodigoPrecio(4);
					precioCosto2.setPrecio(new BigDecimal(v));
					detallesFactura.get(rowIndex).getArticulo().getPreciosVenta().add(precioCosto2);
				}

				break;
			case 11:


				// Define el formato de la cadena de fecha
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

				try {
					// Convierte la cadena de fecha a LocalDate
					LocalDate fecha = LocalDate.parse(v, formatter);

					// Obtiene la fecha actual
					LocalDate fechaActual = LocalDate.now();

					// Compara la fecha ingresada con la fecha actual
					if (fecha.isBefore(fechaActual)) {
						JOptionPane.showMessageDialog(null,"La fecha de vencimientos debe ser una valida.","Error validacion",JOptionPane.ERROR_MESSAGE);
					} else {

							detallesFactura.get(rowIndex).setDateVencimiento(v);
							fireTableCellUpdated(rowIndex, columnIndex);
					}
				} catch (DateTimeParseException e) {
					// Maneja la excepción si la cadena de fecha no tiene el formato correcto
					System.out.println("Error: Formato de fecha incorrecto.");
					e.printStackTrace();
				}

				break;
		}


	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean resul=false;
		if(columnIndex==0)
			resul= true;
		if(columnIndex==1)
			resul=false;
		if(columnIndex==2)
			resul=true;
		if(columnIndex==3)
			resul=true;
		if(columnIndex==4)
			resul=true;
		if(columnIndex==5)
			resul=false;
		if(columnIndex==6)
			resul=false;
		if(columnIndex==6)
			resul=true;

		if(columnIndex==7)
			resul=true;
		if(columnIndex==8)
			resul=true;
		if(columnIndex==9)
			resul=true;
		if(columnIndex==10)
			resul=true;
		if(columnIndex==11)
			resul=true;


		return resul;
	}
	public DetalleFacturaProveedor getDetalles(int index) {
		// TODO Auto-generated method stub
		return detallesFactura.get(index);
	}


	public List<DetalleFacturaProveedor> getDetalles() {
		// TODO Auto-generated method stub
		return detallesFactura;
	}

	public void setArticulo(Articulo a){

		for(int x=0;x<detallesFactura.size();x++){
			if(detallesFactura.get(x).getArticulo().getId()==-1){
				detallesFactura.get(x).setListArticulos(a);
				break;
			}
		}

	}

	public void eliminarDetalle(int index){
		detallesFactura.remove(index);
		fireTableDataChanged();

	}
	public void setImpIncluido(boolean vvv){
		for(int xx=0;xx<detallesFactura.size();xx++){
			if(detallesFactura.get(xx).getArticulo().getId()!=-1){
				detallesFactura.get(xx).setIvaIncludo(vvv);
			}
		}
	}

}
