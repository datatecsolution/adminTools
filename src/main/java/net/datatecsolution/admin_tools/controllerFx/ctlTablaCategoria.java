package net.datatecsolution.admin_tools.controllerFx;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.datatecsolution.admin_tools.modelo.Categoria;
import net.datatecsolution.admin_tools.modelo.dao.CategoriaDao;

import java.util.List;

public class ctlTablaCategoria {
    @FXML
    TableView<Categoria> tDatos;
    @FXML
    TableColumn<Categoria,Integer> tcCodigo;
    @FXML
    TableColumn<Categoria,String> tcDescripcion;
    @FXML
    TableColumn<Categoria,String> tcObservacion;


    @FXML
    public void initialize() {

        tcCodigo.setCellValueFactory(new PropertyValueFactory<Categoria,Integer>("id"));
        tcDescripcion.setCellValueFactory(new PropertyValueFactory<Categoria,String>("descripcion"));
        tcObservacion.setCellValueFactory(new PropertyValueFactory<Categoria,String>("observacion"));

    }

    @FXML
    public void refresh() {

        Task<List<Categoria>> task = new Task<List<Categoria>>() {
            @Override
            protected List<Categoria> call() throws Exception {
                return fetchData();
            }

            @Override
            protected void succeeded() {
                tDatos.getItems().clear();
                tDatos.getItems().addAll( getValue() );
            }
        };

        new Thread(task).start();
    }

    private List<Categoria> fetchData() {
        CategoriaDao categoriaDao=new CategoriaDao();
        List<Categoria> categorias=categoriaDao.todos();

        if(categorias!=null){
            return categorias;
        }else {
            return null;
        }
    }
}
