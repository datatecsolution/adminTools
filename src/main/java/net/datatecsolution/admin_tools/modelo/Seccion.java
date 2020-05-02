package net.datatecsolution.admin_tools.modelo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Seccion {
	
	 @SerializedName("id")
	    @Expose
	    private Integer id=0;
	    @SerializedName("modalidad_id")
	    @Expose
	    private Integer modalidadId;
	    @SerializedName("curso")
	    @Expose
	    private String curso;
	    @SerializedName("seccion")
	    @Expose
	    private String seccion;
	    @SerializedName("jornada")
	    @Expose
	    private String jornada;
	    @SerializedName("created_at")
	    @Expose
	    private String createdAt;
	    @SerializedName("updated_at")
	    @Expose
	    private String updatedAt;

	    @SerializedName("modalidad")
	    @Expose
	    private Modalidad modalidad;

	    public Integer getId() {
	        return id;
	    }

	    public void setId(Integer id) {
	        this.id = id;
	    }

	    public Integer getModalidadId() {
	        return modalidadId;
	    }

	    public void setModalidadId(Integer modalidadId) {
	        this.modalidadId = modalidadId;
	    }

	    public String getCurso() {
	        return curso;
	    }

	    public void setCurso(String curso) {
	        this.curso = curso;
	    }

	    public String getSeccion() {
	        return seccion;
	    }

	    public void setSeccion(String seccion) {
	        this.seccion = seccion;
	    }

	    public String getJornada() {
	        return jornada;
	    }

	    public void setJornada(String jornada) {
	        this.jornada = jornada;
	    }

	    public String getCreatedAt() {
	        return createdAt;
	    }

	    public void setCreatedAt(String createdAt) {
	        this.createdAt = createdAt;
	    }

	    public String getUpdatedAt() {
	        return updatedAt;
	    }

	    public void setUpdatedAt(String updatedAt) {
	        this.updatedAt = updatedAt;
	    }

	    public Modalidad getModalidad() {
	        return modalidad;
	    }

	    public void setModalidad(Modalidad modalidad) {
	        this.modalidad = modalidad;
	    }

	    @Override
	    public String toString() {
	        return "Seccion{" +
	                "id=" + id +
	                ", modalidadId=" + modalidadId +
	                ", curso='" + curso + '\'' +
	                ", seccion='" + seccion + '\'' +
	                ", jornada='" + jornada + '\'' +
	                ", createdAt='" + createdAt + '\'' +
	                ", updatedAt='" + updatedAt + '\'' +
	                ", modalidad=" + modalidad +
	                '}';
	    }

}
