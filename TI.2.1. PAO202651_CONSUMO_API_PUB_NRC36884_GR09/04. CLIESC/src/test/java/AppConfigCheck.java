import com.mycompany.t1_consumo_api_pub_gr09.config.AppConfig;

public class AppConfigCheck {
    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        if (!"jdbc:mysql://localhost/test".equals(config.dbUrl())
                || !"environment-user".equals(config.dbUser())
                || !"password=with\\backslash".equals(config.dbPassword())
                || !"api-key".equals(config.countriesApiKey())) {
            throw new AssertionError("No se cargó la configuración esperada");
        }
    }
}
