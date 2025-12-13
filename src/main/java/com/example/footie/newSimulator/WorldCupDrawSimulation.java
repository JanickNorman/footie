package com.example.footie.newSimulator;

import java.util.ArrayList;
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
        simulator.assignTeamToSlot("E1", "Argentina");
        System.out.println(simulator.state.getDomains().get("F1"));
        simulator.assignTeamToSlot("F1", "Spain");
        // simulator.solveWithBacktracking();

        // Collections.shuffle(teams);
        // teams.sort(Comparator.comparing(Team::pot));
        // teams.forEach(t -> simulator.placeOnlyTeam(t.getName()));
        // simulator.placeOnlyTeam("Brazil");
        // simulator.placeOnlyTeam("Spain");
        // simulator.placeOnlyTeam("Argentina");

        // simulator.tryPlaceTeam("France", 1);
        // simulator.tryPlaceTeam("Germany", 1);
        // simulator.tryPlaceTeam("Brazil", 1);
        // simulator.tryPlaceTeam("Argentina", 1); // Group G
        // simulator.tryPlaceTeam("Spain", 1); // must go directly to J
        // simulator.tryPlaceTeam("Belgium", 1);
        // simulator.tryPlaceTeam("Croatia", 1);
        // simulator.tryPlaceTeam("England", 1);
        // simulator.tryPlaceTeam("Japan", 1);
        // simulator.tryPlaceTeam("Senegal", 3);
        // simulator.tryPlaceTeam("Portugal", 2);
        // simulator.tryPlaceTeam("Norway", 2);
        // simulator.tryPlaceTeam("Curacao", 3);
        // simulator.tryPlaceTeam("Ecuador", 4);
        // simulator.tryPlaceTeam("Switzerland", 3);
        // simulator.tryPlaceTeam("Tunisia", 4);
        // simulator.tryPlaceTeam("Uruguay", 4);
        // simulator.tryPlaceTeam("Ghana", 3);
        // simulator.tryPlaceTeam("SouthKorea", 3);
        // simulator.tryPlaceTeam("Australia");
        // simulator.tryPlaceTeam("CapeVerde");
        // simulator.tryPlaceTeam("European Play-Off A");
        // // simulator.tryPlaceTeam("CotedIvoire", 2);
        // simulator.tryPlaceTeam("Haiti", 4);

        // System.out.println("Remaining groups with most slots: " +
        // simulator.getNextGroupsToAssign());
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