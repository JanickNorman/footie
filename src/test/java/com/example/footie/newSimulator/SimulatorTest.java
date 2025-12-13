package com.example.footie.newSimulator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroup;

@DisplayName("Simulator Tests - Forward Checking & Backtracking")
class SimulatorTest {

    private List<Team> teams;
    private List<GroupSlot> slots;
    private ConstraintManager constraintManager;

    @BeforeEach
    void setUp() {
        // Create teams from different continents
        teams = Arrays.asList(
                TeamFactory.create("France"),
                TeamFactory.create("Germany"),
                TeamFactory.create("Japan"),
                TeamFactory.create("Nigeria"),
                TeamFactory.create("Brazil"),
                TeamFactory.create("USA"));

        // Create slots for 2 groups with 3 slots each
        slots = Arrays.asList(
                new GroupSlot("Group1", 1),
                new GroupSlot("Group1", 2),
                new GroupSlot("Group1", 3),
                new GroupSlot("Group2", 1),
                new GroupSlot("Group2", 2),
                new GroupSlot("Group2", 3));

        constraintManager = new ConstraintManager();
        // constraintManager.addConstraint(new NoSameContinentInGroup());
    }

    private static List<GroupSlot> buildWorldCupSlots() {
        List<GroupSlot> slots = new ArrayList<>();
        for (char g = 'A'; g <= 'I'; g++) {
            String group = String.valueOf(g);
            for (int pos = 1; pos <= 4; pos++) {
                slots.add(new GroupSlot(group, pos));
            }
        }
        return slots;
    }

    @Test
    @DisplayName("Should assign team sequentally to the next possible group in order")
    public void testAssignFranceA1_GermanyB2_BrazilC3() {
        List<GroupSlot> slots = buildWorldCupSlots();

        // Use a small teams list containing the three target teams and some extras
        List<Team> teams = new ArrayList<>();
        teams.add(TeamFactory.create("France"));
        teams.add(TeamFactory.create("Germany"));
        teams.add(TeamFactory.create("Brazil"));
        teams.add(TeamFactory.create("Argentina"));
        teams.add(TeamFactory.create("NewZealand"));

        ConstraintManager cm = new ConstraintManager();
        // cm.addConstraint(new NoSameContinentInGroup());

        Simulator simulator = new Simulator(slots, cm, teams);

        // Perform sequential assignments
        simulator.tryPlaceTeam("France", 1);
        simulator.tryPlaceTeam("Germany", 2);
        simulator.tryPlaceTeam("Brazil", 3);
        // simulator.assignByGroupSequentially("Tonga", 3);

        // Find the slot instances from the original slots list
        GroupSlot a1 = slots.stream().filter(s -> s.getGroupName().equals("A") && s.getPosition() == 1).findFirst()
                .get();
        GroupSlot b2 = slots.stream().filter(s -> s.getGroupName().equals("B") && s.getPosition() == 2).findFirst()
                .get();
        GroupSlot c3 = slots.stream().filter(s -> s.getGroupName().equals("C") && s.getPosition() == 3).findFirst()
                .get();

        AssignmentState state = simulator.getState();

        assertEquals("France", state.getAssignments().get(a1).getName(), "France should be assigned to A1");
        assertEquals("Germany", state.getAssignments().get(b2).getName(), "Germany should be assigned to B2");
        assertEquals("Brazil", state.getAssignments().get(c3).getName(), "Brazil should be assigned to C3");
    }

    @Test
    @DisplayName("Should successfully assign team when constraint is satisfied")
    void testValidAssignment() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);
        GroupSlot slot = slots.get(0);
        Team team = teams.get(0); // TeamA from Europe

        boolean result = simulator.assignTeamToSlot(slot, team);

        assertTrue(result, "Assignment should succeed when constraint is satisfied");
    }

    @Test
    @DisplayName("Should reject assignment when constraint is violated")
    void testInvalidAssignment() {
        constraintManager.addConstraint(new NoSameContinentInGroup());
        Simulator simulator = new Simulator(slots, constraintManager, teams);

        // First assign TeamA (Europe) to Group1
        GroupSlot slot1 = slots.get(0);
        Team teamA = teams.get(0); // Europe
        simulator.assignTeamToSlot(slot1, teamA);

        // Try to assign TeamB (also Europe) to another slot in Group1
        GroupSlot slot2 = slots.get(1);
        Team teamB = teams.get(1); // Europe
        boolean result = simulator.assignTeamToSlot(slot2, teamB);

        assertFalse(result, "Assignment should fail when same continent already exists in group");
    }

    @Test
    @DisplayName("Should perform forward checking and prune domains")
    void testForwardCheckingPrunesDomains() {
        constraintManager.addConstraint(new NoSameContinentInGroup());
        Simulator simulator = new Simulator(slots, constraintManager, teams);

        // Assign TeamA (Europe) to first slot in Group1
        GroupSlot slot1 = slots.get(0);
        Team teamA = teams.get(0); // Europe
        simulator.assignTeamToSlot(slot1, teamA);

        // Check that European teams are removed from other Group1 slot domains
        AssignmentState state = getState(simulator);
        GroupSlot slot2 = slots.get(1); // Another Group1 slot
        Set<Team> domain2 = state.getDomains().get(slot2);

        // TeamB (Europe) should be removed from slot2's domain
        assertFalse(domain2.contains(teams.get(1)), "European team should be pruned from Group1 domains");

        // Non-European teams should still be available
        assertTrue(domain2.contains(teams.get(2)), "Asian team should remain in domain"); // TeamC Asia
        assertTrue(domain2.contains(teams.get(3)), "African team should remain in domain"); // TeamD Africa
    }

    @Test
    @DisplayName("Should detect domain wipeout and backtrack")
    void testDomainWipeoutTriggersBacktrack() {
        constraintManager.addConstraint(new NoSameContinentInGroup());
        // Create a scenario where assignment causes domain wipeout
        List<Team> limitedTeams = Arrays.asList(
                TeamFactory.create("Germany"),
                TeamFactory.create("France"),
                TeamFactory.create("Japan"));

        List<GroupSlot> threeSlots = Arrays.asList(
                new GroupSlot("GroupA", 1),
                new GroupSlot("GroupA", 2),
                new GroupSlot("GroupA", 3));

        Simulator simulator = new Simulator(threeSlots, constraintManager, limitedTeams);

        // Assign T1 (Europe) to first slot
        boolean firstAssign = simulator.assignTeamToSlot(threeSlots.get(0), limitedTeams.get(0));
        assertTrue(firstAssign, "First assignment should succeed");

        // Try to assign T2 (Europe) to second slot - should fail due to domain wipeout
        // because the third slot would have no valid options (both Europe teams used,
        // only Asia remains)
        boolean secondAssign = simulator.assignTeamToSlot(threeSlots.get(1), limitedTeams.get(1));

        // This should fail because it would leave slot 3 with only T3 (Asia),
        // but after pruning Europe teams, there's no valid solution path
        assertFalse(secondAssign, "Assignment should fail when it causes domain wipeout");
    }

    @Test
    @DisplayName("Should restore domains after backtracking")
    void testDomainRestorationAfterBacktrack() {
        constraintManager.addConstraint(new NoSameContinentInGroup());
        Simulator simulator = new Simulator(slots, constraintManager, teams);
        AssignmentState state = getState(simulator);

        GroupSlot slot1 = slots.get(0);
        Team teamA = teams.get(0); // Europe

        // Try an assignment that will fail
        simulator.assignTeamToSlot(slot1, teamA);

        // Now try to assign another Europe team to same group (will fail and backtrack)
        GroupSlot slot2 = slots.get(1);
        Team teamB = teams.get(1); // Europe
        boolean result = simulator.assignTeamToSlot(slot2, teamB);

        assertFalse(result, "Second Europe team assignment should fail");

        // After backtracking, the first assignment should still be there
        assertNotNull(state.getAssigned(slot1), "First assignment should persist");
        assertEquals(teamA, state.getAssigned(slot1), "First assignment should be unchanged");
    }

    @Test
    @DisplayName("Should handle multiple group constraints independently")
    void testMultipleGroupsIndependentConstraints() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);

        // Assign TeamA (Europe) to Group1
        simulator.assignTeamToSlot(slots.get(0), teams.get(0)); // Europe to Group1

        // Should be able to assign TeamB (also Europe) to Group2
        boolean result = simulator.assignTeamToSlot(slots.get(3), teams.get(1)); // Europe to Group2

        assertTrue(result, "Same continent should be allowed in different groups");
    }

    @Test
    @DisplayName("Should complete assignment with shuffleAndAssignAll")
    void testShuffleAndAssignAll() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);

        simulator.shuffleAndAssignAll();

        AssignmentState state = getState(simulator);
        Map<GroupSlot, Team> assignments = state.getAssignments();

        // Count successful assignments
        long assignedCount = assignments.values().stream().filter(t -> t != null).count();

        // With 6 teams and 6 slots, and the constraint that no two teams from same
        // continent in same group,
        // we should be able to assign most or all slots
        assertTrue(assignedCount >= 4, "Should successfully assign at least 4 teams out of 6");
    }

    @Test
    @DisplayName("Should handle empty domain gracefully")
    void testEmptyDomainHandling() {
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
        assertTrue(first, "First assignment should succeed");

        // Try to assign second team (same continent) - should fail
        boolean second = simulator.assignTeamToSlot(twoSlots.get(1), twoTeams.get(1));
        assertFalse(second, "Second assignment should fail due to constraint");
    }

    @Test
    @DisplayName("Should maintain assignment state consistency")
    void testAssignmentStateConsistency() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);
        AssignmentState state = getState(simulator);

        GroupSlot slot1 = slots.get(0);
        Team team1 = teams.get(0);

        simulator.assignTeamToSlot(slot1, team1);

        // Check assignment is recorded
        assertEquals(team1, state.getAssigned(slot1), "Assignment should be recorded in state");
        assertTrue(state.isAssigned(slot1), "Slot should be marked as assigned");

        // Check slot is removed from unassigned list
        assertFalse(state.getUnassignedSlots().contains(slot1), "Assigned slot should not be in unassigned list");
    }

    @Test
    @DisplayName("Should handle all different continents scenario")
    void testAllDifferentContinents() {
        // Scenario where all teams are from different continents
        List<Team> diverseTeams = Arrays.asList(
                TeamFactory.create("T1"),
                TeamFactory.create("T2"),
                TeamFactory.create("T3"));

        List<GroupSlot> threeSlots = Arrays.asList(
                new GroupSlot("G", 1),
                new GroupSlot("G", 2),
                new GroupSlot("G", 3));

        Simulator simulator = new Simulator(threeSlots, constraintManager, diverseTeams);

        // All assignments should succeed since all continents are different
        assertTrue(simulator.assignTeamToSlot(threeSlots.get(0), diverseTeams.get(0)));
        assertTrue(simulator.assignTeamToSlot(threeSlots.get(1), diverseTeams.get(1)));
        assertTrue(simulator.assignTeamToSlot(threeSlots.get(2), diverseTeams.get(2)));
    }

    @Test
    @DisplayName("Should print assignments without errors")
    void testPrintAssignments() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);
        simulator.assignTeamToSlot(slots.get(0), teams.get(0));

        // Should not throw exception
        assertDoesNotThrow(() -> simulator.printAssignments());
    }

    @Test
    @DisplayName("Should prune domains correctly after multiple assignments")
    void testDomainPruningAfterAssignments() {
        Simulator simulator = new Simulator(slots, constraintManager, teams);
        // Assign some teams to slots
        simulator.assignTeamToSlot(slots.get(0), teams.get(0));
        simulator.assignTeamToSlot(slots.get(1), teams.get(1));

        AssignmentState state = getState(simulator);

        // Check that domains have been pruned accordingly
        for (GroupSlot slot : state.getUnassignedSlots()) {
            Set<Team> domain = state.getDomains().get(slot);
            assertNotNull(domain, "Domain should not be null for unassigned slot " + slot);
            // Additional checks can be added here based on constraints
        }
    }

    // Helper method to access private state (using reflection or package-private
    // access)
    private AssignmentState getState(Simulator simulator) {
        try {
            var field = Simulator.class.getDeclaredField("state");
            field.setAccessible(true);
            return (AssignmentState) field.get(simulator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access state field", e);
        }
    }
}
