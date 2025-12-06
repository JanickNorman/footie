package com.example.footie.simulator;

import java.util.*;

public class ExamplePots {

    public static Map<String, List<Team>> makePots() {
        // Minimal POT1 list for demo purposes
        List<Team> pot1 = List.of(
                new Team("England", "Europe"),
                new Team("Spain", "Europe"),
                new Team("Germany", "Europe"),
                new Team("Nigeria", "Africa"),
                new Team("Egypt", "Africa"),
                new Team("Brazil", "South America"),
                new Team("Argentina", "South America"),
                new Team("Japan", "Asia")
        );

        Map<String, List<Team>> pots = new HashMap<>();
        pots.put("POT1", pot1);
        return pots;
    }
}
