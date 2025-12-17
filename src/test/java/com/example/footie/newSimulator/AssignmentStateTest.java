package com.example.footie.newSimulator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AssignmentStateTest {

    @Test
    public void nextSlots_interleavesGroups_A1A4B2B3() {
        List<GroupSlot> slots = List.of(
                new GroupSlot("A", 1),
                new GroupSlot("C", 2),
                new GroupSlot("A", 4),
                new GroupSlot("B", 2),
                new GroupSlot("B", 3));
                

        List<Team> teams = List.of(
                new ConcreteTeam("T1", "Europe", 1),
                new ConcreteTeam("T2", "Europe", 1),
                new ConcreteTeam("T3", "Europe", 1),
                new ConcreteTeam("T4", "Europe", 1));

        AssignmentState state = new AssignmentState(slots, teams);

        List<GroupSlot> next = state.nextSlots();

        List<GroupSlot> expected = List.of(
                new GroupSlot("A", 1),
                new GroupSlot("B", 2),
                new GroupSlot("C", 2),
                new GroupSlot("A", 4),
                new GroupSlot("B", 3));

        assertEquals(expected, next);
    }
}
