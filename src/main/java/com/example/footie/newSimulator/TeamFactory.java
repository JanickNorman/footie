package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TeamFactory {
    public static final Map<String, String> CONTINENT_MAP = new HashMap<>();

    public static final String[][] worldCup2026TeamsByPot = {
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
                        "Euro A", "Euro B",
                        "Euro C", "Euro D",
                        "FIFA 1", "FIFA 2"
                }
        };

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
        CONTINENT_MAP.put("Bolivia", "SouthAmerica");

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
        CONTINENT_MAP.put("Suriname", "NorthAmerica");

        CONTINENT_MAP.put("NewZealand", "Oceania");
        CONTINENT_MAP.put("NewCaledonia", "Oceania");
    }

    private TeamFactory() {
    }

    private static final Map<String, String> NAME_TO_CODE = new HashMap<>();
    static {
        // ISO3 / common football codes for teams used in the lists
        NAME_TO_CODE.put("Canada", "CAN");
        NAME_TO_CODE.put("Mexico", "MEX");
        NAME_TO_CODE.put("USA", "USA");
        NAME_TO_CODE.put("Spain", "ESP");
        NAME_TO_CODE.put("Argentina", "ARG");
        NAME_TO_CODE.put("France", "FRA");
        NAME_TO_CODE.put("England", "ENG");
        NAME_TO_CODE.put("Brazil", "BRA");
        NAME_TO_CODE.put("Portugal", "POR");
        NAME_TO_CODE.put("Netherlands", "NED");
        NAME_TO_CODE.put("Belgium", "BEL");
        NAME_TO_CODE.put("Germany", "GER");
        NAME_TO_CODE.put("Croatia", "CRO");
        NAME_TO_CODE.put("Morocco", "MAR");
        NAME_TO_CODE.put("Colombia", "COL");
        NAME_TO_CODE.put("Uruguay", "URY");
        NAME_TO_CODE.put("Switzerland", "SUI");
        NAME_TO_CODE.put("Japan", "JPN");
        NAME_TO_CODE.put("Senegal", "SEN");
        NAME_TO_CODE.put("Iran", "IRN");
        NAME_TO_CODE.put("SouthKorea", "KOR");
        NAME_TO_CODE.put("Ecuador", "ECU");
        NAME_TO_CODE.put("Austria", "AUT");
        NAME_TO_CODE.put("Australia", "AUS");
        NAME_TO_CODE.put("Norway", "NOR");
        NAME_TO_CODE.put("Panama", "PAN");
        NAME_TO_CODE.put("Egypt", "EGY");
        NAME_TO_CODE.put("Algeria", "DZA");
        NAME_TO_CODE.put("Scotland", "SCO");
        NAME_TO_CODE.put("Paraguay", "PRY");
        NAME_TO_CODE.put("Tunisia", "TUN");
        NAME_TO_CODE.put("CotedIvoire", "CIV");
        NAME_TO_CODE.put("Uzbekistan", "UZB");
        NAME_TO_CODE.put("Qatar", "QAT");
        NAME_TO_CODE.put("SaudiArabia", "SAU");
        NAME_TO_CODE.put("SouthAfrica", "ZAF");
        NAME_TO_CODE.put("Jordan", "JOR");
        NAME_TO_CODE.put("CapeVerde", "CPV");
        NAME_TO_CODE.put("Ghana", "GHA");
        NAME_TO_CODE.put("Curacao", "CUW");
        NAME_TO_CODE.put("Haiti", "HTI");
        NAME_TO_CODE.put("NewZealand", "NZL");
        NAME_TO_CODE.put("Denmark", "DEN");
        NAME_TO_CODE.put("Azerbaijan", "AZE");
        NAME_TO_CODE.put("Wales", "WAL");
        NAME_TO_CODE.put("Hungary", "HUN");
        NAME_TO_CODE.put("Slovakia", "SVK");
        NAME_TO_CODE.put("Serbia", "SRB");
        NAME_TO_CODE.put("RepublicOfIreland", "IRL");
        NAME_TO_CODE.put("Sweden", "SWE");
        NAME_TO_CODE.put("Poland", "POL");
        NAME_TO_CODE.put("Turkey", "TUR");
        NAME_TO_CODE.put("Finland", "FIN");
        NAME_TO_CODE.put("Bolivia", "BOL");
        NAME_TO_CODE.put("Iraq", "IRQ");
        NAME_TO_CODE.put("Suriname", "SUR");
        NAME_TO_CODE.put("DRCongo", "COD");
        NAME_TO_CODE.put("Jamaica", "JAM");
        NAME_TO_CODE.put("NewCaledonia", "NCL");
        
    }

    public static Team create(String name) {
        String continent = CONTINENT_MAP.getOrDefault(name, "Unknown");
        return new ConcreteTeam(name, continent, 1, NAME_TO_CODE.get(name));
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

        for (int pot = 1; pot <= worldCup2026TeamsByPot.length; pot++) {
            for (String teamName : worldCup2026TeamsByPot[pot - 1]) {
                if (teamName.startsWith("Euro A")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("Denmark"), create("Azerbaijan"), create("Wales")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/en/9/9d/UEFA_full_logo.svg"));
                } else if (teamName.startsWith("Euro B")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("Hungary"), create("Norway"), create("Slovakia")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/en/9/9d/UEFA_full_logo.svg"));
                } else if (teamName.startsWith("Euro C")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("Serbia"), create("RepublicOfIreland"), create("Sweden")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/en/9/9d/UEFA_full_logo.svg"));
                } else if (teamName.startsWith("Euro D")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("Poland"), create("Turkey"), create("Finland")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/en/9/9d/UEFA_full_logo.svg"));
                } else if (teamName.equals("FIFA 1")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("Bolivia"), create("Iraq"), create("Suriname")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/commons/1/10/Flag_of_FIFA.svg"));
                } else if (teamName.equals("FIFA 2")) {
                    teams.put(teamName, new PlaceholderTeam(teamName, List.of(create("DRCongo"), create("Jamaica"), create("NewCaledonia")), "placeholder", pot, "https://upload.wikimedia.org/wikipedia/commons/1/10/Flag_of_FIFA.svg"));
                } else {
                    teams.put(teamName, new ConcreteTeam(teamName, CONTINENT_MAP.get(teamName), pot,
                            NAME_TO_CODE.get(teamName)));
                }
            }
        }

        return new ArrayList<>(teams.values());
    }
}
