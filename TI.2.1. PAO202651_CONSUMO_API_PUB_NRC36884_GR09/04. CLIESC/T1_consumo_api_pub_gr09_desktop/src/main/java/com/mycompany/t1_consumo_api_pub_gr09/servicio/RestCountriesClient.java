package com.mycompany.t1_consumo_api_pub_gr09.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mycompany.t1_consumo_api_pub_gr09.config.AppConfig;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RestCountriesClient implements PaisService {
    private static final String API_BASE = "https://api.restcountries.com/countries/v5";
    private static final String BANDERAS_BASE = "https://flags.restcountries.com/v5/w320/";
    private static final String CAMPOS = "names.common,names.official,capitals,codes.alpha_2,codes.alpha_3,region,population,currencies";
    private static final int LIMITE_PAGINA = 100;

    private final AppConfig config;
    private final HttpClient httpClient;

    public RestCountriesClient(AppConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public List<Pais> obtenerTodos() throws IOException, InterruptedException {
        return obtenerPaginas("", null);
    }

    @Override
    public List<Pais> buscarPorNombre(String nombre) throws IOException, InterruptedException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe un nombre antes de consultar la API.");
        }
        return obtenerPaginas("/name", nombre.trim());
    }

    private List<Pais> obtenerPaginas(String endpoint, String consulta) throws IOException, InterruptedException {
        config.requireCountriesApiKey();
        List<Pais> paises = new ArrayList<>();
        Set<String> codigosVistos = new HashSet<>();
        int offset = 0;
        boolean hayMas = true;
        while (hayMas) {
            String query = "?limit=" + LIMITE_PAGINA
                    + "&offset=" + offset
                    + "&response_fields=" + CAMPOS;
            if (consulta != null) {
                query += "&q=" + URLEncoder.encode(consulta, StandardCharsets.UTF_8);
            }
            Page page = solicitar(endpoint + query, offset);
            if (page.paises().isEmpty() && page.hayMas()) {
                throw new IOException("REST Countries indicó que hay más páginas, pero devolvió una página vacía.");
            }
            for (Pais pais : page.paises()) {
                if (!codigosVistos.add(pais.codigoIso())) {
                    throw new IOException("La paginación repitió el código " + pais.codigoIso() + "; no se guardó una importación parcial.");
                }
                paises.add(pais);
            }
            hayMas = page.hayMas();
            if (hayMas) {
                if (page.paises().size() < 1) {
                    throw new IOException("La paginación de REST Countries no avanzó.");
                }
                offset += page.paises().size();
            }
        }
        return List.copyOf(paises);
    }

    private Page solicitar(String path, int offset) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + config.countriesApiKey().trim())
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(mensajeHttp(response.statusCode()));
        }

        JsonObject root = parseObject(response.body());
        if (root.has("errors")) {
            throw new IOException(mensajeErrorApi(root.get("errors")));
        }
        JsonObject data = objeto(root, "data");
        if (data.has("_demo")) {
            throw new IOException("REST Countries devolvió datos de demostración. Configura una API key real; estos datos no se importaron.");
        }
        JsonArray objects = arreglo(data, "objects");
        JsonObject meta = objeto(data, "meta");
        int offsetRespuesta = entero(meta, "offset");
        if (offsetRespuesta != offset) {
            throw new IOException("La respuesta de REST Countries no coincide con la página solicitada.");
        }
        JsonElement more = meta.get("more");
        if (more == null || !more.isJsonPrimitive() || !more.getAsJsonPrimitive().isBoolean()) {
            throw new IOException("REST Countries no devolvió la metadata de paginación esperada.");
        }

        List<Pais> paises = new ArrayList<>(objects.size());
        for (JsonElement element : objects) {
            if (!element.isJsonObject()) {
                throw new IOException("REST Countries devolvió un país con estructura inválida.");
            }
            try {
                paises.add(mapearPais(element.getAsJsonObject()));
            } catch (IllegalArgumentException error) {
                throw new IOException("No se pudo validar un registro de REST Countries: " + error.getMessage(), error);
            }
        }
        return new Page(List.copyOf(paises), more.getAsBoolean());
    }

    private static Pais mapearPais(JsonObject pais) throws IOException {
        JsonObject nombres = objeto(pais, "names");
        JsonObject codigos = objeto(pais, "codes");
        String nombre = texto(nombres.get("common"));
        String codigoIso = texto(codigos.get("alpha_3"));
        String codigoAlpha2 = texto(codigos.get("alpha_2"));
        String region = texto(pais.get("region"));
        if (nombre == null || codigoIso == null || region == null) {
            throw new IllegalArgumentException("faltan nombre común, código ISO alfa-3 o región.");
        }

        String capital = leerCapitales(pais.get("capitals"));
        String moneda = leerMonedas(pais.get("currencies"));
        Long poblacion = leerPoblacion(pais.get("population"));
        String banderaUrl = codigoAlpha2 != null && codigoAlpha2.matches("[A-Za-z]{2}")
                ? BANDERAS_BASE + codigoAlpha2.toLowerCase(Locale.ROOT) + ".png"
                : null;
        return new Pais(0, codigoIso, nombre, capital, region, poblacion, moneda, banderaUrl);
    }

    private static String leerCapitales(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return null;
        }
        List<String> nombres = new ArrayList<>();
        for (JsonElement capital : element.getAsJsonArray()) {
            if (capital.isJsonObject()) {
                String nombre = texto(capital.getAsJsonObject().get("name"));
                if (nombre != null) {
                    nombres.add(nombre);
                }
            }
        }
        return nombres.isEmpty() ? null : String.join("; ", nombres);
    }

    private static String leerMonedas(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        List<String> monedas = new ArrayList<>();
        for (var entry : element.getAsJsonObject().entrySet()) {
            String codigo = entry.getKey();
            String nombre = entry.getValue().isJsonObject()
                    ? texto(entry.getValue().getAsJsonObject().get("name"))
                    : null;
            monedas.add(nombre == null ? codigo : codigo + " — " + nombre);
        }
        return monedas.isEmpty() ? null : String.join("; ", monedas);
    }

    private static Long leerPoblacion(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("la población no es un número entero.");
        }
        try {
            long value = element.getAsBigDecimal().longValueExact();
            if (value < 0) {
                throw new IllegalArgumentException("la población no puede ser negativa.");
            }
            return value;
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("la población supera el rango entero admitido.", error);
        }
    }

    private static JsonObject parseObject(String body) throws IOException {
        try {
            JsonElement json = JsonParser.parseString(body);
            if (!json.isJsonObject()) {
                throw new IOException("REST Countries devolvió JSON que no es un objeto.");
            }
            return json.getAsJsonObject();
        } catch (JsonSyntaxException error) {
            throw new IOException("REST Countries devolvió JSON inválido.", error);
        }
    }

    private static JsonObject objeto(JsonObject parent, String key) throws IOException {
        JsonElement value = parent.get(key);
        if (value == null || !value.isJsonObject()) {
            throw new IOException("La respuesta de REST Countries no contiene " + key + " con el formato esperado.");
        }
        return value.getAsJsonObject();
    }

    private static JsonArray arreglo(JsonObject parent, String key) throws IOException {
        JsonElement value = parent.get(key);
        if (value == null || !value.isJsonArray()) {
            throw new IOException("La respuesta de REST Countries no contiene " + key + " con el formato esperado.");
        }
        return value.getAsJsonArray();
    }

    private static String texto(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? element.getAsString().trim()
                : null;
    }

    private static int entero(JsonObject object, String key) throws IOException {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IOException("La respuesta de REST Countries no contiene la metadata " + key + ".");
        }
        try {
            return value.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException error) {
            throw new IOException("La metadata de paginación es inválida.", error);
        }
    }

    private static String mensajeErrorApi(JsonElement errors) {
        if (errors != null && errors.isJsonArray() && !errors.getAsJsonArray().isEmpty()) {
            JsonElement first = errors.getAsJsonArray().get(0);
            if (first.isJsonObject()) {
                String message = texto(first.getAsJsonObject().get("message"));
                if (message != null) {
                    return "REST Countries: " + message;
                }
            }
        }
        return "REST Countries devolvió un error funcional aunque la petición respondió correctamente.";
    }

    private static String mensajeHttp(int status) {
        return switch (status) {
            case 400 -> "REST Countries rechazó la consulta (HTTP 400). Revisa el texto ingresado.";
            case 401 -> "REST Countries rechazó la API key (HTTP 401). Verifica REST_COUNTRIES_API_KEY.";
            case 403 -> "REST Countries no autorizó la consulta (HTTP 403); revisa permisos o cuota de la cuenta.";
            case 429 -> "REST Countries limitó las peticiones (HTTP 429). Espera antes de volver a consultar.";
            default -> "REST Countries respondió HTTP " + status + ".";
        };
    }

    private record Page(List<Pais> paises, boolean hayMas) {
    }
}
