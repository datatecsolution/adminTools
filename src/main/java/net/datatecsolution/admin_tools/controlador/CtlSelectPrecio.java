package net.datatecsolution.admin_tools.controlador;

import net.datatecsolution.admin_tools.modelo.PrecioArticulo;
import net.datatecsolution.admin_tools.view.ViewSelectPrecio;
import net.datatecsolution.admin_tools.view.tablemodel.CbxTmPrecios;

import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CtlSelectPrecio implements ActionListener, KeyListener {
	
	private final ViewSelectPrecio view;
	private final List<PrecioArticulo> preciosLinea;
	private final List<PrecioArticulo> preciosFactura;
	private PrecioArticulo myPrecio=new PrecioArticulo();
	
	private boolean resultaOperacion=false;
	private boolean aplicarTodo=false;
	
	/**
	 * El combo ofrece solo precios que existen: los del articulo de la linea o,
	 * con "Aplicar a toda la factura", los de todas las lineas (ver {@link #unir}).
	 * Antes listaba todos los tipos de precio y setPrecio() ignoraba en silencio
	 * los que el articulo no tenia.
	 */
	public CtlSelectPrecio(ViewSelectPrecio v, List<PrecioArticulo> preciosLinea, List<PrecioArticulo> preciosFactura){
		view=v;
		this.preciosLinea=preciosLinea;
		this.preciosFactura=preciosFactura;
		
		view.conectarControlador(this);
		view.getChckbxAplicarAToda().setActionCommand("APLICAR_TODO");
		view.getChckbxAplicarAToda().addActionListener(this);
		
		//sin linea seleccionada solo tiene sentido aplicar a toda la factura
		if(preciosLinea==null || preciosLinea.isEmpty()){
			view.getChckbxAplicarAToda().setSelected(true);
			view.getChckbxAplicarAToda().setEnabled(false);
		}
		cargarComboBox();
		
	}
	
	/** Precios distintos (por codigo) de varias lineas, ordenados por codigo. */
	public static List<PrecioArticulo> unir(List<List<PrecioArticulo>> listas){
		Map<Integer,PrecioArticulo> porCodigo=new TreeMap<Integer,PrecioArticulo>();
		for(List<PrecioArticulo> lista:listas){
			if(lista==null) continue;
			for(PrecioArticulo p:lista){
				if(!porCodigo.containsKey(p.getCodigoPrecio()))
					porCodigo.put(p.getCodigoPrecio(),p);
			}
		}
		return new ArrayList<PrecioArticulo>(porCodigo.values());
	}
	
	/** Aviso para las lineas que se quedaron con su precio porque no tienen el elegido. */
	public static void avisarSinCambio(Component padre, int lineas, PrecioArticulo precio){
		if(lineas<=0) return;
		String quien = lineas==1 ? "1 línea no tiene" : lineas+" líneas no tienen";
		JOptionPane.showMessageDialog(padre,
				quien+" el precio \""+precio.getDescripcion()+"\" y conserva su precio anterior.",
				"Precio no aplicado", JOptionPane.WARNING_MESSAGE);
	}
	
	public boolean agregar(){
		view.setVisible(true);
		return resultaOperacion;
	}
	
	

	private void cargarComboBox(){
		List<PrecioArticulo> lista = view.getChckbxAplicarAToda().isSelected() ? preciosFactura : preciosLinea;
		
		CbxTmPrecios modelo=new CbxTmPrecios();
		modelo.setLista(lista);
		view.setModeloPrecioCb(modelo);
		view.getCbPrecios().setModel(modelo);
		
		boolean hay = modelo.getSize()>0;
		view.getBtnGuardar().setEnabled(hay);
		if(hay)
			this.view.getCbPrecios().setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String comando=e.getActionCommand();
		
		switch(comando){
		case "APLICAR_TODO":
			cargarComboBox();
			break;
		case "GUARDAR":
			//Se establece el departamento seleccionado desde la view
			this.setMyPrecio(view.getModeloPrecioCb().getElementAt( view.getCbPrecios().getSelectedIndex()));
			
			//JOptionPane.showMessageDialog(view,this.myPrecio.getDescripcion());
			this.aplicarTodo=view.getChckbxAplicarAToda().isSelected();
			this.resultaOperacion=true;
			view.setVisible(false);
			break;
	
		case "CANCELAR":
			this.resultaOperacion=false;
			view.setVisible(false);
			break;
		
		}
		
	}

	

	private void setMyPrecio(PrecioArticulo selectedItem) {
		// TODO Auto-generated method stub
		this.myPrecio=selectedItem;
		
	}

	private boolean validad() {
		// TODO Auto-generated method stub
		/*
		if(view.getTxtAreaDescripcion().getText().isEmpty()){
			JOptionPane.showMessageDialog(view, "Debe incluir una descripcion de la caja","Error de validacion",JOptionPane.ERROR_MESSAGE);
			return false;
		}*/
		return true;
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the aplicarTodo
	 */
	public boolean isAplicarTodo() {
		return aplicarTodo;
	}

	public PrecioArticulo getPrecioSelect() {
		return myPrecio;
	}



}
