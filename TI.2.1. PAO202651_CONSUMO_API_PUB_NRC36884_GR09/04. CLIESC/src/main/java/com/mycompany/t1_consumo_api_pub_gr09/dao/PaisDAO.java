package com.mycompany.t1_consumo_api_pub_gr09.dao;

import com.mycompany.t1_consumo_api_pub_gr09.config.AppConfig;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.EstadisticasPaises;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.ResultadoImportacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PaisDAO {
    private static final String COLUMNAS = "id, codigo_iso, nombre, capital, region, poblacion, moneda, bandera_url";
    private static final String INSERTAR = "INSERT INTO pais (codigo_iso, nombre, capital, region, poblacion, moneda, bandera_url) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final AppConfig config;

    public PaisDAO(AppConfig config) {
        this.config = config;
    }

    public List<Pais> listar(String nombre, String region) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNAS).append(" FROM pais WHERE 1 = 1");
        List<String> parametros = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sql.append(" AND nombre LIKE ? ESCAPE '!'");
            parametros.add("%" + escaparLike(nombre.trim()) + "%");
        }
        if (region != null && !region.isBlank() && !"Todas".equals(region)) {
            sql.append(" AND region = ?");
            parametros.add(region);
        }
        sql.append(" ORDER BY nombre");

        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < parametros.size(); index++) {
                statement.setString(index + 1, parametros.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Pais> paises = new ArrayList<>();
                while (resultSet.next()) {
                    paises.add(leerPais(resultSet));
                }
                return paises;
            }
        }
    }

    public Optional<Pais> buscarPorId(int id) throws SQLException {
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNAS + " FROM pais WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(leerPais(resultSet)) : Optional.empty();
            }
        }
    }

    public int crear(Pais pais) throws SQLException {
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            enlazarPais(statement, pais);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("MySQL no devolvió el ID generado para el país.");
                }
                return keys.getInt(1);
            }
        }
    }

    public boolean actualizar(Pais pais) throws SQLException {
        String sql = "UPDATE pais SET nombre = ?, capital = ?, region = ?, poblacion = ?, moneda = ?, bandera_url = ? WHERE id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pais.nombre());
            setNullableString(statement, 2, pais.capital());
            statement.setString(3, pais.region());
            setNullableLong(statement, 4, pais.poblacion());
            setNullableString(statement, 5, pais.moneda());
            setNullableString(statement, 6, pais.banderaUrl());
            statement.setInt(7, pais.id());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM pais WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public ResultadoImportacion importar(List<Pais> paises) throws SQLException {
        String sql = INSERTAR;
        int insertados = 0;
        int omitidos = 0;
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement existe = connection.prepareStatement("SELECT 1 FROM pais WHERE codigo_iso = ?");
                 PreparedStatement insertar = connection.prepareStatement(sql)) {
                for (Pais pais : paises) {
                    existe.setString(1, pais.codigoIso());
                    try (ResultSet resultSet = existe.executeQuery()) {
                        if (resultSet.next()) {
                            omitidos++;
                            continue;
                        }
                    }
                    enlazarPais(insertar, pais);
                    insertar.executeUpdate();
                    insertados++;
                }
                connection.commit();
                return new ResultadoImportacion(paises.size(), insertados, omitidos, 0);
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
        }
    }

    public List<String> listarRegiones() throws SQLException {
        String sql = "SELECT DISTINCT region FROM pais ORDER BY region";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<String> regiones = new ArrayList<>();
            while (resultSet.next()) {
                regiones.add(resultSet.getString(1));
            }
            return regiones;
        }
    }

    public EstadisticasPaises obtenerEstadisticas() throws SQLException {
        long total = 0;
        Map<String, Long> porRegion = new LinkedHashMap<>();
        String sql = "SELECT region, COUNT(*) AS cantidad FROM pais GROUP BY region ORDER BY region";
        try (Connection connection = config.openConnection();
             PreparedStatement totalStatement = connection.prepareStatement("SELECT COUNT(*) FROM pais");
             ResultSet totalResult = totalStatement.executeQuery()) {
            if (totalResult.next()) {
                total = totalResult.getLong(1);
            }
            try (PreparedStatement regionStatement = connection.prepareStatement(sql);
                 ResultSet regionResult = regionStatement.executeQuery()) {
                while (regionResult.next()) {
                    porRegion.put(regionResult.getString("region"), regionResult.getLong("cantidad"));
                }
            }
            Long poblacionMaxima = null;
            List<String> paisesMasPoblados = new ArrayList<>();
            try (PreparedStatement maxStatement = connection.prepareStatement("SELECT MAX(poblacion) FROM pais");
                 ResultSet maxResult = maxStatement.executeQuery()) {
                if (maxResult.next()) {
                    long value = maxResult.getLong(1);
                    if (!maxResult.wasNull()) {
                        poblacionMaxima = value;
                    }
                }
            }
            if (poblacionMaxima != null) {
                try (PreparedStatement namesStatement = connection.prepareStatement(
                        "SELECT nombre FROM pais WHERE poblacion = ? ORDER BY nombre")) {
                    namesStatement.setLong(1, poblacionMaxima);
                    try (ResultSet namesResult = namesStatement.executeQuery()) {
                        while (namesResult.next()) {
                            paisesMasPoblados.add(namesResult.getString(1));
                        }
                    }
                }
            }
            long maxRegion = porRegion.values().stream().mapToLong(Long::longValue).max().orElse(0);
            List<String> regionesDominantes = porRegion.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxRegion)
                    .map(Map.Entry::getKey)
                    .toList();
            return new EstadisticasPaises(total, porRegion, poblacionMaxima, paisesMasPoblados, regionesDominantes);
        }
    }

    private static Pais leerPais(ResultSet resultSet) throws SQLException {
        long poblacion = resultSet.getLong("poblacion");
        Long poblacionOpcional = resultSet.wasNull() ? null : poblacion;
        return new Pais(
                resultSet.getInt("id"),
                resultSet.getString("codigo_iso"),
                resultSet.getString("nombre"),
                resultSet.getString("capital"),
                resultSet.getString("region"),
                poblacionOpcional,
                resultSet.getString("moneda"),
                resultSet.getString("bandera_url")
        );
    }

    private static void enlazarPais(PreparedStatement statement, Pais pais) throws SQLException {
        statement.setString(1, pais.codigoIso());
        statement.setString(2, pais.nombre());
        setNullableString(statement, 3, pais.capital());
        statement.setString(4, pais.region());
        setNullableLong(statement, 5, pais.poblacion());
        setNullableString(statement, 6, pais.moneda());
        setNullableString(statement, 7, pais.banderaUrl());
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }

    private static String escaparLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
