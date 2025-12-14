package com.example.footie.newSimulator.constraint;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.ConcreteTeam;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

public class PairedGroupConstraintTest {

    private Team teamA;
    private Team teamB;
    private Team teamC;
    private GroupSlot slotA1;
    private GroupSlot slotB1;
    private GroupSlot slotC1;
    private GroupSlot slotA2;
    private GroupSlot slotB2;
    private GroupSlot slotC2;
    private Set<String> groupsA;
    private Set<String> groupsB;
    private AssignmentState state;

    @BeforeEach
    void setUp() {
        teamA = new ConcreteTeam("TeamA", "SouthAmerica", 1);
        teamB = new ConcreteTeam("TeamB", "Europe", 1);
        teamC = new ConcreteTeam("TeamC", "Africa", 1);

        slotA1 = new GroupSlot("A", 1);
        slotB1 = new GroupSlot("B", 1);
        slotC1 = new GroupSlot("C", 1);
        slotA2 = new GroupSlot("A", 2);
        slotB2 = new GroupSlot("B", 2);
        slotC2 = new GroupSlot("C", 2);

        groupsA = Set.of("A", "B");
        groupsB = Set.of("C");

        state = new AssignmentState(List.of(slotA1, slotB1, slotC1, slotA2, slotB2, slotC2), List.of(teamA, teamB, teamC));
    }

    @Test
    void testIsAssignmentAllowedNonSymmetric() {
        PairedGroupConstraint constraint = new PairedGroupConstraint(teamA.getName(), groupsA, teamB.getName(), groupsB, false);

        // Scenario 1: TeamA assigned to a group in groupsA, TeamB not yet assigned. Should be allowed for now.
        assertTrue(constraint.isAssignmentAllowed(state, slotA1, teamA));

        // Assign TeamA to A1
        state.assign(slotA1, teamA);

        // Scenario 2: TeamB attempts to assign to a group NOT in groupsB. Should be disallowed.
        assertFalse(constraint.isAssignmentAllowed(state, slotA2, teamB));

        // Scenario 3: TeamB attempts to assign to a group in groupsB. Should be allowed.
        assertTrue(constraint.isAssignmentAllowed(state, slotC1, teamB));
    }

    @Test
    void testIsAssignmentAllowedSymmetric() {
        PairedGroupConstraint constraint = new PairedGroupConstraint(teamA.getName(), groupsA, teamB.getName(), groupsB, true);

        // Assign TeamA to A1
        state.assign(slotA1, teamA);

        // Scenario 1: TeamB attempts to assign to a group NOT in groupsB. Should be disallowed.
        assertFalse(constraint.isAssignmentAllowed(state, slotA2, teamB));

        // Scenario 2: TeamB attempts to assign to a group in groupsB. Should be allowed.
        assertTrue(constraint.isAssignmentAllowed(state, slotC1, teamB));

        // Reset state for symmetric check
        setUp();

        // Assign TeamB to C1
        state.assign(slotC1, teamB);

        // Scenario 3: TeamA attempts to assign to a group NOT in groupsA. Should be disallowed (symmetric rule).
        assertFalse(constraint.isAssignmentAllowed(state, slotC2, teamA));

        // Scenario 4: TeamA attempts to assign to a group in groupsA. Should be allowed.
        assertTrue(constraint.isAssignmentAllowed(state, slotA1, teamA));
    }

    @Test
    void testForwardCheckNonSymmetric() {
        PairedGroupConstraint constraint = new PairedGroupConstraint(teamA.getName(), groupsA, teamB.getName(), groupsB, false);

        // Assign TeamA to A1 (a group in groupsA)
        state.assign(slotA1, teamA);
        constraint.forwardCheck(state, slotA1, teamA);

        // Domain of TeamB should now be pruned to only groupsB
        Set<Team> domainOfTeamBInSlotA2 = state.getDomains().get(slotA2);
        assertFalse(domainOfTeamBInSlotA2.contains(teamB), "TeamB should be pruned from slot A2 (not in groupsB).");

        Set<Team> domainOfTeamBInSlotC1 = state.getDomains().get(slotC1);
        assertTrue(domainOfTeamBInSlotC1.contains(teamB), "TeamB should still be in slot C1 (in groupsB).");

        // Assign TeamA to C1 (a group NOT in groupsA). No pruning should occur for TeamB related to groupsB.
        setUp();
        state.assign(slotC1, teamA);
        constraint.forwardCheck(state, slotC1, teamA);

        domainOfTeamBInSlotA2 = state.getDomains().get(slotA2);
        assertTrue(domainOfTeamBInSlotA2.contains(teamB), "TeamB should NOT be pruned from slot A2 if TeamA is not in groupsA.");

        domainOfTeamBInSlotC1 = state.getDomains().get(slotC1);
        assertTrue(domainOfTeamBInSlotC1.contains(teamB), "TeamB should still be in slot C1.");
    }

    @Test
    void testForwardCheckSymmetric() {
        PairedGroupConstraint constraint = new PairedGroupConstraint(teamA.getName(), groupsA, teamB.getName(), groupsB, true);

        // Scenario 1: Assign TeamA to A1 (in groupsA)
        state.assign(slotA1, teamA);
        constraint.forwardCheck(state, slotA1, teamA);

        // TeamB should be pruned to only groupsB
        Set<Team> domainOfTeamBInSlotA2 = state.getDomains().get(slotA2);
        assertFalse(domainOfTeamBInSlotA2.contains(teamB), "TeamB should be pruned from slot A2 (not in groupsB).");
        Set<Team> domainOfTeamBInSlotC1 = state.getDomains().get(slotC1);
        assertTrue(domainOfTeamBInSlotC1.contains(teamB), "TeamB should still be in slot C1 (in groupsB).");

        // Scenario 2: Reset and assign TeamB to C1 (in groupsB)
        setUp();
        state.assign(slotC1, teamB);
        constraint.forwardCheck(state, slotC1, teamB);

        // TeamA should be pruned to only groupsA
        Set<Team> domainOfTeamAInSlotC2 = state.getDomains().get(slotC2);
        assertFalse(domainOfTeamAInSlotC2.contains(teamA), "TeamA should be pruned from slot C2 (not in groupsA).");
        Set<Team> domainOfTeamAInSlotA1 = state.getDomains().get(slotA1);
        assertTrue(domainOfTeamAInSlotA1.contains(teamA), "TeamA should still be in slot A1 (in groupsA).");

        // Scenario 3: Reset and assign TeamA to C1 (NOT in groupsA). Symmetric constraint should prune TeamB to *exclude* groupsB
        setUp();
        state.assign(slotC1, teamA);
        constraint.forwardCheck(state, slotC1, teamA);

        // TeamB should be pruned from all groups in groupsB
        domainOfTeamBInSlotC1 = state.getDomains().get(slotC1);
        assertFalse(domainOfTeamBInSlotC1.contains(teamB), "TeamB should be pruned from slot C1 (groupsB) due to symmetric rule.");
        Set<Team> domainOfTeamBInSlotA2_sym = state.getDomains().get(slotA2);
        assertTrue(domainOfTeamBInSlotA2_sym.contains(teamB), "TeamB should NOT be pruned from slot A2 (not in groupsB) due to symmetric rule.");

        // Scenario 4: Reset and assign TeamB to A1 (NOT in groupsB). Symmetric constraint should prune TeamA to *exclude* groupsA
        setUp();
        state.assign(slotA1, teamB);
        constraint.forwardCheck(state, slotA1, teamB);

        // TeamA should be pruned from all groups in groupsA
        domainOfTeamAInSlotA1 = state.getDomains().get(slotA1);
        assertFalse(domainOfTeamAInSlotA1.contains(teamA), "TeamA should be pruned from slot A1 (groupsA) due to symmetric rule.");
        Set<Team> domainOfTeamAInSlotC2_sym = state.getDomains().get(slotC2);
        assertTrue(domainOfTeamAInSlotC2_sym.contains(teamA), "TeamA should NOT be pruned from slot C2 (not in groupsA) due to symmetric rule.");
    }
}
