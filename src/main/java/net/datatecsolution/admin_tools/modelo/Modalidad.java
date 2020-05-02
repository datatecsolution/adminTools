package net.datatecsolution.admin_tools.modelo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Modalidad {
	 @SerializedName("id")
	    @Expose
	    private Integer id;
	    @SerializedName("alias")
	    @Expose
	    private String alias;
	    @SerializedName("nombre")
	    @Expose
	    private String nombre;
	    @SerializedName("observaciones")
	    @Expose
	    private String observaciones;
	    @SerializedName("created_at")
	    @Expose
	    private String createdAt;
	    @SerializedName("updated_at")
	    @Expose
	    private String updatedAt;

	    public Integer getId() {
	        return id;
	    }

	    public void setId(Integer id) {
	        this.id = id;
	    }

	    public String getAlias() {
	        return alias;
	    }

	    public void setAlias(String alias) {
	        this.alias = alias;
	    }

	    public String getNombre() {
	        return nombre;
	    }

	    public void setNombre(String nombre) {
	        this.nombre = nombre;
	    }

	    public String getObservaciones() {
	        return observaciones;
	    }

	    public void setObservaciones(String observaciones) {
	        this.observaciones = observaciones;
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

	    @Override
	    public String toString() {
	        return "Modalidad{" +
	                "id=" + id +
	                ", alias='" + alias + '\'' +
	                ", nombre='" + nombre + '\'' +
	                ", observaciones='" + observaciones + '\'' +
	                ", createdAt='" + createdAt + '\'' +
	                ", updatedAt='" + updatedAt + '\'' +
	                '}';
	    }

}
