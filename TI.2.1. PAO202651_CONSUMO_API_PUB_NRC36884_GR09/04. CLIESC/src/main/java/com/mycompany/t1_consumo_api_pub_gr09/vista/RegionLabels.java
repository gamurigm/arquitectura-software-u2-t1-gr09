package com.mycompany.t1_consumo_api_pub_gr09.vista;

import java.util.Locale;

final class RegionLabels {
    private RegionLabels() {
    }

    static String mostrar(String region) {
        if (region == null) {
            return "Sin región";
        }
        return switch (region.trim().toLowerCase(Locale.ROOT)) {
            case "americas", "america", "américa" -> "América";
            case "africa", "áfrica" -> "África";
            case "europe", "europa" -> "Europa";
            case "oceania", "oceanía" -> "Oceanía";
            case "antarctic", "antártida", "antartida" -> "Antártida";
            case "asia" -> "Asia";
            default -> region;
        };
    }

    static String canonica(String region) {
        if (region == null) {
            return "";
        }
        return switch (region.trim().toLowerCase(Locale.ROOT)) {
            case "américa", "america", "americas" -> "Americas";
            case "áfrica", "africa" -> "Africa";
            case "europa", "europe" -> "Europe";
            case "oceanía", "oceania" -> "Oceania";
            case "antártida", "antartida", "antarctic" -> "Antarctic";
            case "asia" -> "Asia";
            default -> region.trim();
        };
    }
}
