package com.mycompany.t1_consumo_api_pub_gr09.modelo;

import java.net.URI;
import java.util.Locale;

public record Pais(
        int id,
        String codigoIso,
        String nombre,
        String capital,
        String region,
        Long poblacion,
        String moneda,
        String banderaUrl
) {

    public Pais {
        codigoIso = normalizar(codigoIso).toUpperCase(Locale.ROOT);
        nombre = normalizar(nombre);
        capital = normalizarOpcional(capital);
        region = normalizar(region);
        moneda = normalizarOpcional(moneda);
        banderaUrl = normalizarOpcional(banderaUrl);

        if (id < 0) {
            throw new IllegalArgumentException("El ID no puede ser negativo.");
        }
        if (!codigoIso.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("El código ISO debe tener tres letras.");
        }
        exigirTexto(nombre, "El nombre es obligatorio.", 100, "El nombre supera 100 caracteres.");
        exigirTexto(region, "La región es obligatoria.", 100, "La región supera 100 caracteres.");
        exigirLongitud(capital, 255, "La capital supera 255 caracteres.");
        exigirLongitud(moneda, 255, "La moneda supera 255 caracteres.");
        exigirLongitud(banderaUrl, 512, "La URL de bandera supera 512 caracteres.");
        if (poblacion != null && poblacion < 0) {
            throw new IllegalArgumentException("La población no puede ser negativa.");
        }
        if (banderaUrl != null) {
            URI uri = URI.create(banderaUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"flags.restcountries.com".equalsIgnoreCase(uri.getHost())) {
                throw new IllegalArgumentException("La URL de bandera debe pertenecer al CDN HTTPS de REST Countries.");
            }
        }
    }

    public Pais conId(int nuevoId) {
        return new Pais(nuevoId, codigoIso, nombre, capital, region, poblacion, moneda, banderaUrl);
    }

    private static String normalizar(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizarOpcional(String value) {
        String normalizado = normalizar(value);
        return normalizado.isEmpty() ? null : normalizado;
    }

    private static void exigirTexto(String value, String mensajeVacio, int maximo, String mensajeLargo) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(mensajeVacio);
        }
        exigirLongitud(value, maximo, mensajeLargo);
    }

    private static void exigirLongitud(String value, int maximo, String mensaje) {
        if (value != null && value.length() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
