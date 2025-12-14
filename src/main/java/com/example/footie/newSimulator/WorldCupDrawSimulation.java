package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.example.footie.newSimulator.constraint.AtMostTwoEuropeTeamsPerGroup;
import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroupForNonEurope;
import com.example.footie.newSimulator.constraint.PairedGroupConstraint;
import com.example.footie.newSimulator.constraint.SamePotCantBeInTheSameGroup;

public class WorldCupDrawSimulation {
    public static void main(String[] args) {
        // test();
        List<Team> teams = TeamFactory.createWorldCupTeams(4);

        List<GroupSlot> slots = buildWorldCupSlots();
        // teams = teams.stream().filter(s -> s.getContinents().size() == 0).toList();

        ConstraintManager cm = new ConstraintManager();
        cm.addConstraint(new SamePotCantBeInTheSameGroup());
        cm.addConstraint(new AtMostTwoEuropeTeamsPerGroup());
        cm.addConstraint(new NoSameContinentInGroupForNonEurope());
        cm.addConstraint(new PairedGroupConstraint("Spain", Set.of("E", "I", "F", "H", "D", "G"), "Argentina",
                Set.of("C", "A", "L", "J", "B", "K"), true));

        Simulator simulator = new Simulator(slots, cm, teams);

        // give me idea to assignTeams sequentally
        simulator.assignTeamToSlot("A1", "Mexico");
        simulator.assignTeamToSlot("B1", "Canada");
        simulator.assignTeamToSlot("D1", "USA");
        simulator.placeOnlyTeam("Brazil");
        simulator.placeOnlyTeam("Argentina");
        simulator.placeOnlyTeam("Spain");
        simulator.placeOnlyTeam("Portugal");
        simulator.placeOnlyTeam("Netherlands");
        simulator.placeOnlyTeam("France");
        simulator.placeOnlyTeam("Germany");
        simulator.placeOnlyTeam("Brazil");
        simulator.placeOnlyTeam("Belgium");
        simulator.placeOnlyTeam("England");
        
        Collections.shuffle(teams);
        teams.stream().filter(t -> t.pot() == 2).forEach(t -> {
            simulator.placeOnlyTeam(t.getName());
        });
        teams.stream().filter(t -> t.pot() == 3).forEach(t -> {
            if (t.getContinents().contains("Europe")) {
                simulator.placeOnlyTeam(t.getName());
                return;
            }
            simulator.placeOnlyTeam(t.getName());
        });
        teams.stream().filter(t -> t.pot() == 4).forEach(t -> {
            simulator.placeOnlyTeam(t.getName());
        });

        simulator.prettyPrintGroupAssignmentsVertical();
    }

    private static List<GroupSlot> buildWorldCupSlots() {
        List<GroupSlot> slots = new ArrayList<>();
        // Groups A..I, positions 1..4
        for (char g = 'A'; g <= 'L'; g++) {
            String group = String.valueOf(g);
            for (int pos = 1; pos <= 4; pos++) {
                slots.add(new GroupSlot(group, pos));
            }
        }
        return slots;
    }
}