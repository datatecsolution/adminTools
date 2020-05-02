package net.datatecsolution.admin_tools.view.botones;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class BotonesApp extends JButton {
	private static final int ancho=128;
	private static final int alto=45;
	private static Dimension dim=new Dimension(ancho,alto);
	private static Font myFon;
	public boolean add=false;
	
	public static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
	    Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  Image.SCALE_SMOOTH);
	    return new ImageIcon(resizedImage);
	}
	
	public BotonesApp(){
		this.setSize(ancho, alto);
		myFon=new Font("Georgia", Font.PLAIN, 13);
		this.setFont(myFon);
		Color color1 =new Color(60, 179, 113);
		setBackground(color1);
		//this.setPreferredSize(dim);
		
	}
	public BotonesApp(String titulo){
		super(titulo);
		this.setSize(ancho, alto);
		myFon=new Font("Georgia", Font.PLAIN, 13);
		this.setFont(myFon);
		Color color1 =new Color(60, 179, 113);
		setBackground(color1);
		
	}
	public void setTituto(String titulo){
		this.setText(titulo);
	}
	

}
