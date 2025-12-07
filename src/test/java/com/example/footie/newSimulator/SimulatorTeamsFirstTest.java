package com.example.footie.newSimulator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SimulatorTeamsFirstTest {
    @Test
    public void testAssignByGroupSequentially_assignByGroupOrder() {
        List<Team> teams = Arrays.asList(
            TeamFactory.create("France"),
            TeamFactory.create("Germany"),
            TeamFactory.create("Brazil"),
            TeamFactory.create("Japan"),
            TeamFactory.create("Egypt"),
            TeamFactory.create("Spain"),
            TeamFactory.create("Argentina")
        );

        List<GroupSlot> slots = Arrays.asList(
            new GroupSlot("A", 1),
            new GroupSlot("A", 2),
            new GroupSlot("B", 1),
            new GroupSlot("B", 2),
            new GroupSlot("C", 1),
            new GroupSlot("C", 2),
            new GroupSlot("D", 1)
        );

        ConstraintManager cm = new ConstraintManager();
        // cm.addConstraint(new NoSameContinentInGroup());

        Simulator simulator = new Simulator(slots, cm, teams);
        simulator.assignByGroupSequentially("France", 1);
        simulator.assignByGroupSequentially("Germany", 1);

        // France should be assigned to Group A slot 1 && Germany to Group B slot 1
        assertEquals("France", simulator.getState().getAssignments().get(slots.get(0)).getName());
        assertEquals("Germany", simulator.getState().getAssignments().get(slots.get(2)).getName());
        simulator.printAssignments();        
    }

    @Test
    @DisplayName("Assignment should have alldifferent constraint by default")
    public void testAssignByGroupSequentially_alldifferentConstraint() {
        List<Team> teams = Arrays.asList(
            TeamFactory.create("France"),
            TeamFactory.create("Germany"),
            TeamFactory.create("Brazil"),
            TeamFactory.create("Japan"),
            TeamFactory.create("Egypt"),
            TeamFactory.create("Spain"),
            TeamFactory.create("Argentina")
        );

        List<GroupSlot> slots = Arrays.asList(
            new GroupSlot("A", 1),
            new GroupSlot("A", 2),
            new GroupSlot("B", 1),
            new GroupSlot("B", 2),
            new GroupSlot("C", 1),
            new GroupSlot("C", 2),
            new GroupSlot("D", 1)
        );

        ConstraintManager cm = new ConstraintManager();
        // cm.addConstraint(new NoSameContinentInGroup());

        Simulator simulator = new Simulator(slots, cm, teams);
        boolean firstAssignmentResult = simulator.assignTeamToSlot(slots.get(0), teams.get(2));
        boolean secondAssignmentResult = simulator.assignTeamToSlot(slots.get(2), teams.get(2));

        // The second assignment of Brazil should fail due to alldifferent constraint
        assertFalse(secondAssignmentResult, "Alldifferent constraint violated: same team assigned twice.");
    }
}
