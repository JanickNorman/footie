package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.footie.newSimulator.constraint.AllDifferent;
import com.example.footie.newSimulator.constraint.AtMostTwoEuropeTeamsPerGroup;
import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroupForNonEurope;
import com.example.footie.newSimulator.constraint.SamePotCantBeInTheSameGroup;
import com.example.footie.newSimulator.constraint.TopSeedsBracketSeparation;

public class WorldCupDrawSimulation {
    public static void main(String[] args) {
        // test();
        List<Team> teams = TeamFactory.createWorldCupTeams(4);

        List<GroupSlot> slots = buildWorldCupSlots();
        // teams = teams.stream().filter(s -> s.getContinents().size() == 0).toList();

        ConstraintManager cm = new ConstraintManager();
        cm.addConstraint(new AllDifferent());
        cm.addConstraint(new SamePotCantBeInTheSameGroup());
        cm.addConstraint(new AtMostTwoEuropeTeamsPerGroup());
        cm.addConstraint(new NoSameContinentInGroupForNonEurope());
        cm.addConstraint(new TopSeedsBracketSeparation(Map.of(
                "Argentina", 1,
                "Spain", 2,
                "France", 3,
                "England", 4
        )));
        // simulator.assignTeamToSlot("A1", "Mexico");
        // simulator.assignTeamToSlot("B1", "Canada");
        // simulator.assignTeamToSlot("D1", "USA");
        // simulator.placeOnlyTeam("Brazil");
        // simulator.placeOnlyTeam("Germany");
        // simulator.placeOnlyTeam("Netherlands");
        // simulator.placeOnlyTeam("Belgium");
        // simulator.placeOnlyTeam("Spain");
        // simulator.placeOnlyTeam("Argentina");
        // simulator.placeOnlyTeam("France");
        // simulator.placeOnlyTeam("Portugal");
        // simulator.placeOnlyTeam("England");

        // simulator.placeOnlyTeam("SouthKorea");
        // simulator.placeOnlyTeam("Switzerland");
        // simulator.placeOnlyTeam("Morocco");
        // simulator.placeOnlyTeam("Australia");
        // simulator.placeOnlyTeam("Ecuador");
        // simulator.placeOnlyTeam("Japan");
        // simulator.placeOnlyTeam("Iran");
        // simulator.placeOnlyTeam("Uruguay");
        // simulator.placeOnlyTeam("Senegal");
        // simulator.placeOnlyTeam("Austria");
        // simulator.placeOnlyTeam("Colombia");
        // simulator.placeOnlyTeam("Croatia");

        // simulator.placeOnlyTeam("SouthAfrica");
        // simulator.placeOnlyTeam("Qatar");
        // simulator.placeOnlyTeam("Paraguay");
        // simulator.placeOnlyTeam("CotedIvoire");
        // simulator.placeOnlyTeam("Tunisia");
        // simulator.placeOnlyTeam("Egypt");
        // simulator.placeOnlyTeam("Scotland");
        // simulator.placeOnlyTeam("SaudiArabia");
        // simulator.placeOnlyTeam("Algeria");
        // simulator.placeOnlyTeam("Uzbekistan");
        // simulator.placeOnlyTeam("Panama");
        // simulator.placeOnlyTeam("Norway");
        // Collections.shuffle(teams);
        // teams.stream().filter(t -> t.pot() == 2).forEach(t -> simulator.placeTeam(t.getName()));
        // teams.stream().filter(t -> t.pot() == 3).forEach(t -> simulator.placeTeam(t.getName()));
        // teams.stream().filter(t -> t.pot() == 4).forEach(t -> simulator.placeTeam(t.getName()));
        // simulator.makePlacements();
        // simulator.solveWithBacktracking();
        // simulator.solveWorldCup2026Draw();


        Simulator simulator = new Simulator(slots, cm, teams);
        simulator.solveWorldCup2026Draw();
                // simulator.shuffleAndSolve();
        simulator.prettyPrintGroupAssignmentsVertical();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        // for (int i = 0; i < 5; i++) {
        //     final int threadIndex = i;
        //     futures.add(executor.submit(() -> {
        //         String threadName = Thread.currentThread().getName();
        //         Simulator simulator = new Simulator(slots, cm, teams);
        //         simulator.solveWorldCup2026Draw();
        //         // simulator.shuffleAndSolve();
        //         System.out.println(threadIndex + " Running simulation on thread: " + threadName);
        //         simulator.prettyPrintGroupAssignmentsVertical();
        //     }));
        // }

        // for (Future<?> future : futures) {
        //     try {
        //         future.get();
        //     } catch (InterruptedException | ExecutionException e) {
        //         e.printStackTrace();
        //     }
        // }

        // executor.shutdown();


        

        

    
        // simulator.makePlacements();
        // System.out.println(simulator.getState().findUnassignedTeamsWithNoUnassignedCandidateSlot());
    

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