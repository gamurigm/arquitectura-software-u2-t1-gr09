package com.mycompany.t1_consumo_api_pub_gr09.modelo;

import java.util.List;
import java.util.Map;

public record EstadisticasPaises(
        long totalPaises,
        Map<String, Long> paisesPorRegion,
        Long poblacionMaxima,
        List<String> paisesMasPoblados,
        List<String> regionesConMasPaises
) {
    public EstadisticasPaises {
        paisesPorRegion = Map.copyOf(paisesPorRegion);
        paisesMasPoblados = List.copyOf(paisesMasPoblados);
        regionesConMasPaises = List.copyOf(regionesConMasPaises);
    }
}
