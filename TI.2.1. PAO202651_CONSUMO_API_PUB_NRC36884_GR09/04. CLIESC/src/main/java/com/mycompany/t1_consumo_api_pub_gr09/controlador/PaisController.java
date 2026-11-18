package com.mycompany.t1_consumo_api_pub_gr09.controlador;

import com.mycompany.t1_consumo_api_pub_gr09.dao.PaisDAO;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.EstadisticasPaises;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.ResultadoImportacion;
import com.mycompany.t1_consumo_api_pub_gr09.servicio.PaisService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public final class PaisController {
    private final PaisDAO paisDAO;
    private final PaisService countriesClient;

    public PaisController(PaisDAO paisDAO, PaisService countriesClient) {
        this.paisDAO = paisDAO;
        this.countriesClient = countriesClient;
    }

    public List<Pais> listar(String nombre, String region) throws SQLException {
        return paisDAO.listar(nombre, region);
    }

    public List<String> listarRegiones() throws SQLException {
        return paisDAO.listarRegiones();
    }

    public EstadisticasPaises obtenerEstadisticas() throws SQLException {
        return paisDAO.obtenerEstadisticas();
    }

    public List<Pais> buscarEnApi(String nombre) throws IOException, InterruptedException {
        return countriesClient.buscarPorNombre(nombre);
    }

    public ResultadoImportacion importarPaises() throws IOException, InterruptedException, SQLException {
        List<Pais> paises = countriesClient.obtenerTodos();
        if (paises.isEmpty()) {
            throw new IOException("REST Countries no devolvió países; no se realizó ninguna escritura.");
        }
        return paisDAO.importar(paises);
    }

    public ResultadoImportacion guardarResultadoApi(Pais pais) throws SQLException {
        return paisDAO.importar(List.of(pais));
    }

    public int crear(Pais pais) throws SQLException {
        validarPoblacionManual(pais);
        return paisDAO.crear(pais);
    }

    public boolean actualizar(Pais pais) throws SQLException {
        validarPoblacionManual(pais);
        return paisDAO.actualizar(pais);
    }

    public boolean eliminar(int id) throws SQLException {
        return paisDAO.eliminar(id);
    }

    private static void validarPoblacionManual(Pais pais) {
        if (pais.poblacion() == null) {
            throw new IllegalArgumentException("La población es obligatoria para crear o editar un país.");
        }
    }
}
