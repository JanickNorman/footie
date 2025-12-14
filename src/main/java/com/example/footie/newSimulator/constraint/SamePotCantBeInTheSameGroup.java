package com.example.footie.newSimulator.constraint;

import java.util.Objects;
import java.util.Set;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

public class SamePotCantBeInTheSameGroup implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        if (slot == null || slot.getGroupName() == null || team == null) {
            return true;
        }

        return state.getAssignments().entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().getGroupName() != null)
                .filter(e -> e.getKey().getGroupName().equals(slot.getGroupName()))
                .map(e -> e.getValue())
                .filter(Objects::nonNull)
                .noneMatch(t -> t.pot() == team.pot());
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        // When a team is placed into a group, remove any teams from the same pot
        // from the domains of other unassigned slots in the same group.
        if (slot == null || slot.getGroupName() == null || team == null) {
            return;
        }

        state.getUnassignedSlots().stream()
                .filter(s -> s != null && s.getGroupName() != null && s.getGroupName().equals(slot.getGroupName()))
                .forEach(s -> {
                    Set<Team> domain = state.getDomains(s);
                    if (domain != null) {
                        domain.removeIf(t -> t != null && t.pot() == team.pot());
                    }
                });
    }   

}
