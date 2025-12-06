package com.example.footie.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        // same teams and domains as before
        Map<String, List<Team>> pots = ExamplePots.makePots();
        Map<String, Set<Team>> domains = CSPUtils.initializeDomains(pots);

        List<Constraint> constraints = List.of(
                // new NoSameContinentConstraint(),
                new GroupCMustBeAfrican()
        );

        WorldCupDrawSimulator simulator = new WorldCupDrawSimulator(domains, constraints);

        // Simulate a draw (Pot 1)
        List<Team> pot1 = new ArrayList<>(pots.get("POT1"));
        Collections.shuffle(pot1);

        for (Team t : pot1) {
            simulator.assignTeamSequentially(t);
            
        }

        // Print final assignments
        System.out.println("\nFINAL DRAW RESULT:");
        simulator.getAssignments().forEach((k,v) ->
                System.out.println(k + " → " + v.getName() + " (" + v.getContinent() + ")"));
    }
}
