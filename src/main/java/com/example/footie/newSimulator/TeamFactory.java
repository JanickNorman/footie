package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TeamFactory {
    public static final Map<String, String> CONTINENT_MAP = new HashMap<>();

    static {
        CONTINENT_MAP.put("Germany", "Europe");
        CONTINENT_MAP.put("Spain", "Europe");
        CONTINENT_MAP.put("France", "Europe");
        CONTINENT_MAP.put("Portugal", "Europe");
        CONTINENT_MAP.put("England", "Europe");
        CONTINENT_MAP.put("Netherlands", "Europe");
        CONTINENT_MAP.put("Belgium", "Europe");
        CONTINENT_MAP.put("Croatia", "Europe");
        CONTINENT_MAP.put("Serbia", "Europe");
        CONTINENT_MAP.put("Scotland", "Europe");
        CONTINENT_MAP.put("Turkey", "Europe");
        CONTINENT_MAP.put("Austria", "Europe");
        CONTINENT_MAP.put("Hungary", "Europe");
        CONTINENT_MAP.put("Poland", "Europe");
        CONTINENT_MAP.put("Italy", "Europe");
        CONTINENT_MAP.put("Switzerland", "Europe");

        CONTINENT_MAP.put("Brazil", "SouthAmerica");
        CONTINENT_MAP.put("Argentina", "SouthAmerica");
        CONTINENT_MAP.put("Uruguay", "SouthAmerica");
        CONTINENT_MAP.put("Colombia", "SouthAmerica");
        CONTINENT_MAP.put("Ecuador", "SouthAmerica");
        CONTINENT_MAP.put("Peru", "SouthAmerica");

        CONTINENT_MAP.put("Egypt", "Africa");
        CONTINENT_MAP.put("Nigeria", "Africa");
        CONTINENT_MAP.put("Senegal", "Africa");
        CONTINENT_MAP.put("Morocco", "Africa");
        CONTINENT_MAP.put("Cameroon", "Africa");
        CONTINENT_MAP.put("Mali", "Africa");
        CONTINENT_MAP.put("Algeria", "Africa");
        CONTINENT_MAP.put("Tunisia", "Africa");
        CONTINENT_MAP.put("SouthAfrica", "Africa");

        CONTINENT_MAP.put("Japan", "Asia");
        CONTINENT_MAP.put("SouthKorea", "Asia");
        CONTINENT_MAP.put("Australia", "Asia");
        CONTINENT_MAP.put("China", "Asia");
        CONTINENT_MAP.put("Iran", "Asia");
        CONTINENT_MAP.put("SaudiArabia", "Asia");
        CONTINENT_MAP.put("Qatar", "Asia");
        CONTINENT_MAP.put("Iraq", "Asia");

        CONTINENT_MAP.put("USA", "NorthAmerica");
        CONTINENT_MAP.put("Canada", "NorthAmerica");
        CONTINENT_MAP.put("Mexico", "NorthAmerica");
        CONTINENT_MAP.put("CostaRica", "NorthAmerica");
        CONTINENT_MAP.put("Panama", "NorthAmerica");
        CONTINENT_MAP.put("Honduras", "NorthAmerica");
        CONTINENT_MAP.put("Jamaica", "NorthAmerica");
        CONTINENT_MAP.put("ElSalvador", "NorthAmerica");

        CONTINENT_MAP.put("NewZealand", "Oceania");

        // generic placeholders
    }

    private TeamFactory() {}

    public static Team create(String name) {
        String continent = CONTINENT_MAP.getOrDefault(name, "Unknown");
        return new ConcreteTeam(name, continent);
    }
    public static Team createPlaceholder(String placeholderName) {
        // default continent for placeholders is Unknown unless mapped
        if (placeholderName.contains("Euro Playoff")) {
            return new PlaceholderTeam(placeholderName, Set.of("Europe"), "placeholder");
        }
        return new PlaceholderTeam(placeholderName, Set.of("Asia", "Africa"), "placeholder");
    }

    public static List<Team> createWorldCupPlaceholderTeams() {
        List<Team> teams = new ArrayList<>();
        CONTINENT_MAP.forEach((name, continent) -> {
            teams.add(new ConcreteTeam(name, continent));
        });
        return teams;
    }
}
