package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.footie.newSimulator.constraint.AllDifferent;
import com.example.footie.newSimulator.constraint.AtMostTwoEuropeTeamsPerGroup;
import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroupForNonEurope;
import com.example.footie.newSimulator.constraint.SamePotCantBeInTheSameGroup;
import com.example.footie.newSimulator.constraint.TopSeedsBracketSeparation;

public class WorldCupDrawSimulation {
    public static void main(String[] args) {
        List<Team> teams = TeamFactory.createWorldCupTeams(4);
        List<GroupSlot> slots = buildWorldCupSlots();

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

        Simulator simulator = new Simulator(slots, cm, teams);
        simulator.solveWorldCup2026Draw();
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