package net.datatecsolution.admin_tools.modelo.dao;

import javafx.collections.ObservableList;
import net.datatecsolution.admin_tools.modelo.Seccion;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface ApiService {
	@GET("secciones/{id}")
    ObservableList<Seccion> getSeccion(@Path("id") Integer id);
}
