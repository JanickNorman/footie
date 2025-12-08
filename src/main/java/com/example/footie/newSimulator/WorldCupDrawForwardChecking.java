package com.example.footie.newSimulator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroup;

public class WorldCupDrawForwardChecking {
    public static void main(String[] args) {
        // test();
        List<Team> teams = TeamFactory.createWorldCupTeams(4);

        List<GroupSlot> slots = buildWorldCupSlots();
        // teams = teams.stream().filter(s -> s.getContinents().size() == 0).toList();

        ConstraintManager cm = new ConstraintManager();
        Simulator simulator = new Simulator(slots, cm, teams);

        //give me idea to assignTeams sequentally
        simulator.assignByGroupSequentially("France", 1);
        simulator.assignByGroupSequentially("Germany", 2);
        simulator.assignByGroupSequentially("Brazil", 4);
        simulator.assignByGroupSequentially("Argentina", 1);
        simulator.assignByGroupSequentially("NewZealand", 3);
        // simulator.assignByGroupSequentially("Uruguay", 4);
        // simulator.assignTeamToGroup("B", teams.get(6));
        // simulator.shuffleAndAssignAll();
        simulator.prettyPrintGroupAssignments();
    }

    private static void test() {
        ConstraintManager constraintManager = new ConstraintManager();
        constraintManager.addConstraint(new NoSameContinentInGroup());
        // Create impossible scenario: 3 slots in one group, only 2 continents
        List<Team> twoTeams = Arrays.asList(
            TeamFactory.create("Germany"),
            TeamFactory.create("France"),
            TeamFactory.create("Japan")
        );

        List<GroupSlot> twoSlots = Arrays.asList(
            new GroupSlot("G", 1),
            new GroupSlot("G", 2)
        );

        Simulator simulator = new Simulator(twoSlots, constraintManager, twoTeams);

        // Assign first team
        boolean first = simulator.assignTeamToSlot(twoSlots.get(0), twoTeams.get(0));
        System.out.println("First assignment (should be true): " + first);

        // Attempt to assign second team (should fail due to continent constraint)
        boolean second = simulator.assignTeamToSlot(twoSlots.get(1), twoTeams.get(1));
        System.out.println("Second assignment (should be false): " + second);
    }

    private static List<GroupSlot> buildWorldCupSlots() {
        List<GroupSlot> slots = new ArrayList<>();
        // Groups A..I, positions 1..4
        for (char g = 'A'; g <= 'I'; g++) {
            String group = String.valueOf(g);
            for (int pos = 1; pos <= 4; pos++) {
                slots.add(new GroupSlot(group, pos));
            }
        }
        return slots;
    }
}