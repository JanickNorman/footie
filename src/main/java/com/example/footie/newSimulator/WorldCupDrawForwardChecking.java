package com.example.footie.newSimulator;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class WorldCupDrawForwardChecking {
    public static void main(String[] args) {
        List<Team> teams = TeamFactory.createWorldCupPlaceholderTeams();

        List<GroupSlot> slots = buildWorldCupSlots();

        ConstraintManager cm = new ConstraintManager();
        Simulator simulator = new Simulator(slots, cm, teams);

        //give me idea to assignTeams sequentally
        simulator.assignByGroupSequentially("France", 1);
        simulator.assignByGroupSequentially("Germany", 1);
        simulator.assignByGroupSequentially("Brazil", 1);
        simulator.assignByGroupSequentially("Argentina", 1);
        simulator.assignByGroupSequentially("NewZealand", 4);
        // simulator.assignTeamToGroup("B", teams.get(6));
        // simulator.shuffleAndAssignAll();
        simulator.prettyPrintGroupAssignments();
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