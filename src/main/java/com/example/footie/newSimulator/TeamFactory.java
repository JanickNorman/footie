package com.example.footie.newSimulator;

import java.util.HashMap;
import java.util.Map;

public final class TeamFactory {
    private static final Map<String, String> CONTINENT_MAP = new HashMap<>();

    static {
        CONTINENT_MAP.put("France", "Europe");
        CONTINENT_MAP.put("Germany", "Europe");
        CONTINENT_MAP.put("Spain", "Europe");
        CONTINENT_MAP.put("Egypt", "Africa");
        CONTINENT_MAP.put("Nigeria", "Africa");
        CONTINENT_MAP.put("Senegal", "Africa");
        CONTINENT_MAP.put("Brazil", "SouthAmerica");
        CONTINENT_MAP.put("Argentina", "SouthAmerica");
        CONTINENT_MAP.put("USA", "North America");
        CONTINENT_MAP.put("Japan", "Asia");
        // generic placeholders
        CONTINENT_MAP.put("T1", "Europe");
        CONTINENT_MAP.put("T2", "Europe");
        CONTINENT_MAP.put("T3", "Asia");
        CONTINENT_MAP.put("A", "Europe");
        CONTINENT_MAP.put("B", "Europe");
    }

    private TeamFactory() {}

    public static Team create(String name) {
        String continent = CONTINENT_MAP.getOrDefault(name, "Unknown");
        return new ConcreteTeam(name, continent);
    }
    public static Team createPlaceholder(String placeholderName) {
        // default continent for placeholders is Unknown unless mapped
        String continent = CONTINENT_MAP.getOrDefault(placeholderName, "Unknown");

        return new PlaceholderTeam(placeholderName, continent, "placeholder");
    }
}
