package com.mycompany.t1_consumo_api_pub_gr09.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public record AppConfig(String dbUrl, String dbUser, String dbPassword, String countriesApiKey) {

    public static AppConfig fromEnvironment() {
        return new AppConfig(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD"),
                System.getenv("REST_COUNTRIES_API_KEY")
        );
    }

    public Connection openConnection() throws SQLException {
        if (isBlank(dbUrl) || isBlank(dbUser) || dbPassword == null) {
            throw new SQLException("Configura DB_URL, DB_USER y DB_PASSWORD en el entorno.");
        }
        Properties properties = new Properties();
        properties.setProperty("user", dbUser);
        properties.setProperty("password", dbPassword);
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "15000");
        return DriverManager.getConnection(dbUrl, properties);
    }

    public void requireCountriesApiKey() {
        if (isBlank(countriesApiKey)) {
            throw new IllegalStateException("Configura REST_COUNTRIES_API_KEY para consultar REST Countries.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
