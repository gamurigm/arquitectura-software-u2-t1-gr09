package com.mycompany.t1_consumo_api_pub_gr09;

import com.mycompany.t1_consumo_api_pub_gr09.config.AppConfig;
import com.mycompany.t1_consumo_api_pub_gr09.controlador.PaisController;
import com.mycompany.t1_consumo_api_pub_gr09.dao.PaisDAO;
import com.mycompany.t1_consumo_api_pub_gr09.servicio.RestCountriesClient;
import com.mycompany.t1_consumo_api_pub_gr09.vista.AtlasFrame;
import com.mycompany.t1_consumo_api_pub_gr09.vista.AtlasTheme;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AtlasTheme.instalar();
            AppConfig config = AppConfig.fromEnvironment();
            PaisController controller = new PaisController(
                    new PaisDAO(config),
                    new RestCountriesClient(config)
            );
            AtlasFrame frame = new AtlasFrame(controller);
            frame.setVisible(true);
            frame.refrescarCatalogo();
        });
    }
}
