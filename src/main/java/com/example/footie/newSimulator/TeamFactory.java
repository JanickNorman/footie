package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import tools.jackson.core.json.JsonReadContext;

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
        CONTINENT_MAP.put("Denmark", "Europe");
        CONTINENT_MAP.put("Norway", "Europe");

        CONTINENT_MAP.put("Brazil", "SouthAmerica");
        CONTINENT_MAP.put("Argentina", "SouthAmerica");
        CONTINENT_MAP.put("Chile", "SouthAmerica");
        CONTINENT_MAP.put("Uruguay", "SouthAmerica");
        CONTINENT_MAP.put("Colombia", "SouthAmerica");
        CONTINENT_MAP.put("Ecuador", "SouthAmerica");
        CONTINENT_MAP.put("Peru", "SouthAmerica");
        CONTINENT_MAP.put("Venezuela", "SouthAmerica");
        CONTINENT_MAP.put("Paraguay", "SouthAmerica");

        CONTINENT_MAP.put("Egypt", "Africa");
        CONTINENT_MAP.put("Nigeria", "Africa");
        CONTINENT_MAP.put("Senegal", "Africa");
        CONTINENT_MAP.put("Morocco", "Africa");
        CONTINENT_MAP.put("Cameroon", "Africa");
        CONTINENT_MAP.put("Mali", "Africa");
        CONTINENT_MAP.put("Algeria", "Africa");
        CONTINENT_MAP.put("Tunisia", "Africa");
        CONTINENT_MAP.put("SouthAfrica", "Africa");
        CONTINENT_MAP.put("CotedIvoire", "Africa");
        CONTINENT_MAP.put("Ghana", "Africa");
        CONTINENT_MAP.put("CapeVerde", "Africa");

        CONTINENT_MAP.put("Japan", "Asia");
        CONTINENT_MAP.put("SouthKorea", "Asia");
        CONTINENT_MAP.put("Australia", "Asia");
        CONTINENT_MAP.put("China", "Asia");
        CONTINENT_MAP.put("Iran", "Asia");
        CONTINENT_MAP.put("SaudiArabia", "Asia");
        CONTINENT_MAP.put("Qatar", "Asia");
        CONTINENT_MAP.put("Iraq", "Asia");
        CONTINENT_MAP.put("Jordan", "Asia");
        CONTINENT_MAP.put("Uzbekistan", "Asia");

        CONTINENT_MAP.put("USA", "NorthAmerica");
        CONTINENT_MAP.put("Canada", "NorthAmerica");
        CONTINENT_MAP.put("Mexico", "NorthAmerica");
        CONTINENT_MAP.put("CostaRica", "NorthAmerica");
        CONTINENT_MAP.put("Panama", "NorthAmerica");
        CONTINENT_MAP.put("Honduras", "NorthAmerica");
        CONTINENT_MAP.put("Jamaica", "NorthAmerica");
        CONTINENT_MAP.put("ElSalvador", "NorthAmerica");
        CONTINENT_MAP.put("Haiti", "NorthAmerica");
        CONTINENT_MAP.put("Curacao", "NorthAmerica");

        CONTINENT_MAP.put("NewZealand", "Oceania");
    }

    private TeamFactory() {
    }

    public static Team create(String name) {
        String continent = CONTINENT_MAP.getOrDefault(name, "Unknown");
        return new ConcreteTeam(name, continent, 1);
    }

    public static Team createPlaceholder(String placeholderName, int pot) {
        // default continent for placeholders is Unknown unless mapped
        if (placeholderName.contains("Euro Playoff")) {
            return new PlaceholderTeam(placeholderName, Set.of("Europe"), "placeholder", pot);
        }

        if (placeholderName.contains("FIFA Playoff 1")) {
            return new PlaceholderTeam(placeholderName, Set.of("SouthAmerica", "Asia", "NorthAmerica"), "placeholder",
                    pot);
        }

        return new PlaceholderTeam(placeholderName, Set.of("Africa", "NorthAmerica", "Oceania"), "placeholder", pot);
    }

    public static List<Team> createWorldCupTeams(final int total_pot) {
        Map<String, Team> teams = new HashMap<>();

        String[][] worldCup2026TeamsByPot = {
                {
                        "Canada", "Mexico", "USA",
                        "Spain", "Argentina", "France",
                        "England", "Brazil", "Portugal",
                        "Netherlands", "Belgium", "Germany"
                },
                {
                        "Croatia", "Morocco", "Colombia",
                        "Uruguay", "Switzerland", "Japan",
                        "Senegal", "Iran", "SouthKorea",
                        "Ecuador", "Austria", "Australia"
                },
                {
                        "Norway", "Panama", "Egypt",
                        "Algeria", "Scotland", "Paraguay",
                        "Tunisia", "CotedIvoire", "Uzbekistan",
                        "Qatar", "SaudiArabia", "SouthAfrica"
                },
                {
                        "Jordan", "CapeVerde", "Ghana",
                        "Curacao", "Haiti", "NewZealand",
                        "European Play-Off A", "European Play-Off B",
                        "European Play-Off C", "European Play-Off D",
                        "FIFA Play-Off 1", "FIFA Play-Off 2"
                }
        };

        for (int pot = 1; pot <= worldCup2026TeamsByPot.length; pot++) {
            for (String teamName : worldCup2026TeamsByPot[pot - 1]) {
                if (teamName.startsWith("European Play-Off")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, Set.of("Europe"), "placeholder", pot));
                } else if (teamName.equals("FIFA Play-Off 1")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, Set.of("SouthAmerica", "Asia", "NorthAmerica"),
                            "placeholder", pot));
                } else if (teamName.equals("FIFA Play-Off 2")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, Set.of("Africa", "NorthAmerica", "Oceania"),
                            "placeholder", pot));
                } else {
                    teams.put(teamName, new ConcreteTeam(teamName, CONTINENT_MAP.get(teamName), pot));
                }
            }
        }

        return new ArrayList<>(teams.values());
    }
}
