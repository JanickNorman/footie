package com.example.footie.simulator;

import java.util.*;

public class CSPUtils {

    /**
     * Initialize domains for slots A1, B1, C1 using POT1 teams as possible values.
     */
    public static Map<String, Set<Team>> initializeDomains(Map<String, List<Team>> pots) {
        Map<String, Set<Team>> domains = new HashMap<>();
        List<Team> pot1 = pots.getOrDefault("POT1", List.of());

        // for this simple simulator we only create three slots: A1, B1, C1
        List<String> slots = List.of("A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1");

        for (String s : slots) {
            domains.put(s, new HashSet<>(pot1));
        }

        return domains;
    }
}
