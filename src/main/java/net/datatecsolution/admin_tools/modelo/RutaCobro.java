package net.datatecsolution.admin_tools.modelo;

public class RutaCobro {

    private int codigo=0;
    private String descripcion="";
    private String obser="";


    public RutaCobro(){

    }

    public RutaCobro(int codigo, String descripcion, String obser) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.obser = obser;
    }

    public String getObser() {
        return obser;
    }

    public void setObser(String obser) {
        this.obser = obser;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString(){
        return this.codigo+" | "+this.descripcion;
    }

}
