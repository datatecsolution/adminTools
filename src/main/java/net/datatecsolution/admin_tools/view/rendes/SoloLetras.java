package net.datatecsolution.admin_tools.view.rendes;

import java.awt.Robot;

import javax.swing.*;

import java.awt.event.*;
public class SoloLetras extends JFrame {
     
    private JTextField caja = new JTextField();
     
    public SoloLetras() {
    super("Solo dejo escribir letras");
    caja.addKeyListener(new KeyAdapter() {
         
    public void keyReleased(KeyEvent e)
    {
        Character caracter = new Character(e.getKeyChar());
        if (!esValido(caracter))
        {
           String texto = "";
           for (int i = 0; i < caja.getText().length(); i++)
                if (esValido(new Character(caja.getText().charAt(i))))
                    texto += caja.getText().charAt(i);
                caja.setText(texto);
                //getToolkit().beep();
        }
    }
             
    public boolean esValido(Character caracter)
            {
                char c = caracter.charValue();
                return Character.isLetter(c) //si es letra
                        || c == ' ' //o un espacio
                        || c == 8 //o backspace
                        || (Character.isDigit(c));
            }
        });
        getContentPane().add(caja);
        setSize(300, 60);
        setVisible(true);
    }
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SoloLetras ventana = new SoloLetras();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}