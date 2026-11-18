import com.mycompany.t1_consumo_api_pub_gr09.config.AppConfig;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import com.mycompany.t1_consumo_api_pub_gr09.servicio.RestCountriesClient;
import java.util.HashSet;
import java.util.List;

public class RestCountriesCheck {
    public static void main(String[] args) throws Exception {
        var catalogo = new RestCountriesClient(AppConfig.fromEnvironment()).obtenerTodos();
        List<Pais> paises = catalogo.paises();
        if (paises.size() < 200 || paises.stream().noneMatch(pais -> pais.codigoIso().equals("ECU"))
                || catalogo.sinCodigoIso() < 1
                || new HashSet<>(paises.stream().map(Pais::codigoIso).toList()).size() != paises.size()) {
            throw new AssertionError("El catálogo no contiene los países esperados sin duplicados");
        }
    }
}
