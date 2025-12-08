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
        cm.addConstraint(new NoSameContinentInGroup());
        Simulator simulator = new Simulator(slots, cm, teams);

        // give me idea to assignTeams sequentally
        simulator.assignTeamToSlot("A1", "Mexico");
        simulator.assignTeamToSlot("B1", "Canada");
        simulator.assignTeamToSlot("D1", "USA");

        simulator.tryPlaceTeam("France", 1);
        simulator.tryPlaceTeam("Germany", 2);
        simulator.tryPlaceTeam("Brazil", 4);
        simulator.tryPlaceTeam("Argentina", 1);
        simulator.tryPlaceTeam("Belgium", 1);
        simulator.tryPlaceTeam("Croatia", 1);
        simulator.tryPlaceTeam("England", 2);
        simulator.tryPlaceTeam("Morocco", 2);
        simulator.tryPlaceTeam("Japan", 3);
        simulator.tryPlaceTeam("Senegal", 3);
        simulator.tryPlaceTeam("Portugal", 2);
        simulator.tryPlaceTeam("Norway", 2);
        simulator.tryPlaceTeam("Curacao", 3);
        simulator.tryPlaceTeam("Ecuador", 4);
        simulator.tryPlaceTeam("Switzerland", 3);
        simulator.tryPlaceTeam("Tunisia", 4);
        simulator.tryPlaceTeam("Uruguay", 4);
        simulator.tryPlaceTeam("Ghana", 3);
        simulator.tryPlaceTeam("SouthKorea", 3);
        // simulator.tryPlaceTeam("CotedIvoire", 2);
        simulator.tryPlaceTeam("Haiti", 4);

        // simulator.tryPlaceTeam("Place", 2);

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
                TeamFactory.create("Japan"));

        List<GroupSlot> twoSlots = Arrays.asList(
                new GroupSlot("G", 1),
                new GroupSlot("G", 2));

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
        for (char g = 'A'; g <= 'L'; g++) {
            String group = String.valueOf(g);
            for (int pos = 1; pos <= 4; pos++) {
                slots.add(new GroupSlot(group, pos));
            }
        }
        return slots;
    }
}