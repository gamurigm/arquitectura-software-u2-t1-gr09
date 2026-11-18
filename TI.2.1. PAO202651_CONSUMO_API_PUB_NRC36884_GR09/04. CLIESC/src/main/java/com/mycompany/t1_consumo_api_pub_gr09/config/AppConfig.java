package com.mycompany.t1_consumo_api_pub_gr09.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public record AppConfig(String dbUrl, String dbUser, String dbPassword, String countriesApiKey) {

    public static AppConfig fromEnvironment() {
        Path envFile = Path.of("../.env");
        if (Files.notExists(envFile)) {
            envFile = Path.of("../../.env");
        }
        if (Files.notExists(envFile)) {
            envFile = Path.of(".env");
        }
        Properties fileValues = new Properties();
        if (!Files.notExists(envFile)) {
            try {
                for (String line : Files.readAllLines(envFile)) {
                    int separator = line.indexOf('=');
                    if (separator > 0 && !line.stripLeading().startsWith("#")) {
                        fileValues.setProperty(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo leer el archivo .env", ex);
            }
        }
        return new AppConfig(
                System.getenv().getOrDefault("DB_URL", fileValues.getProperty("DB_URL")),
                System.getenv().getOrDefault("DB_USER", fileValues.getProperty("DB_USER")),
                System.getenv().getOrDefault("DB_PASSWORD", fileValues.getProperty("DB_PASSWORD")),
                System.getenv().getOrDefault("REST_COUNTRIES_API_KEY", fileValues.getProperty("REST_COUNTRIES_API_KEY"))
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
