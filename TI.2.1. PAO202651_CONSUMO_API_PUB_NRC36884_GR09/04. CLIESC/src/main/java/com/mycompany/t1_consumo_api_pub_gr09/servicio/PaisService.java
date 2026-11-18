package com.mycompany.t1_consumo_api_pub_gr09.servicio;

import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import java.io.IOException;
import java.util.List;

public interface PaisService {
    Catalogo obtenerTodos() throws IOException, InterruptedException;

    List<Pais> buscarPorNombre(String nombre) throws IOException, InterruptedException;

    record Catalogo(List<Pais> paises, int sinCodigoIso) {
    }
}
