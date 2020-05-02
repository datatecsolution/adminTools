package net.datatecsolution.admin_tools.view.tablemodel;

import javax.swing.table.AbstractTableModel;

public abstract class TablaModelo extends AbstractTableModel{

	private static int cantItemPag=20;
	private int limiteSuperior=0;
	private int noPagina=1;
	
	public int getCanItemPag(){
		return cantItemPag;
	}
	public int getLimiteSuperior(){
		return limiteSuperior;
	}
	public int getNoPagina(){
		return noPagina;
	}
	
	public void netPag(){
		//cantItemPag+=20;
		limiteSuperior+=20;
		noPagina++;
	}
	public void lastPag(){
		
		if((limiteSuperior-20)>0){
		//	cantItemPag-=20;
			limiteSuperior-=20;
			noPagina--;
		}else{
				setPaginacion();
		}
	}
	public void setPaginacion(){
		limiteSuperior=0;
		noPagina=1;
		
	}

}
