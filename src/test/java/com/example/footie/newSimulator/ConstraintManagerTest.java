package com.example.footie.newSimulator;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import com.example.footie.newSimulator.constraint.ConstraintManager;

public class ConstraintManagerTest {

    @Test
    public void testForwardCheckingPrunesDomains() {

        GroupSlot slot1 = new GroupSlot("A", 1);
        GroupSlot slot2 = new GroupSlot("A", 2);
        Team team1 = TeamFactory.create("Germany");
        Team team2 = TeamFactory.create("Japan");
        Team team3 = TeamFactory.create("Senegal");
        AssignmentState state = new AssignmentState(List.of(slot1, slot2), List.of(team1, team2, team3));
        ConstraintManager cm = new ConstraintManager();

        System.out.println("Initial domain of slot2: " + state.getDomains().get(slot2));
        cm.forwardCheck(state, slot1, team1);
        System.out.println("Domain of slot2 after forward checking: " + state.getDomains().get(slot2));

        Set<Team> domainSlot2 = state.getDomains().get(slot2);
        assertFalse(domainSlot2.contains(team1), "Domain of slot2 should not contain team1 after forward checking.");
    }
}
